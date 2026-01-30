(ns ^:deprecated http-client-component.with-httpkit-client
  "This namespace is deprecated. Please use http-client-component.with-hato instead."
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [http-client-component.models.targets :as models.targets]
            [iapetos.core :as prometheus]
            [integrant.core :as ig]
            [medley.core :as medley]
            [org.httpkit.client :as hk-client]
            [schema.core :as s]))

(def method->request-fn
  {:post   ^:clj-kondo/ignore hk-client/post
   :get    ^:clj-kondo/ignore hk-client/get
   :put    ^:clj-kondo/ignore hk-client/put
   :patch  ^:clj-kondo/ignore hk-client/patch
   :delete ^:clj-kondo/ignore hk-client/delete})

(defmulti request!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defn http-response-metric!
  [service :- s/Str
   status :- s/Int
   endpoint-id :- s/Str
   prometheus-registry :- s/Any]
  (prometheus/inc prometheus-registry :http-request-response {:status   status
                                                              :service  service
                                                              :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}))

(s/defn http-response-timing-metric!
  [service :- s/Str
   endpoint-id :- s/Str
   start-ms :- s/Int
   prometheus-registry :- s/Any]
  (prometheus/observe prometheus-registry :http-request-response-timing
                      {:service  service
                       :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}
                      (- (System/currentTimeMillis) start-ms)))

(s/defmethod request! :prod
  [{:keys [method endpoint target payload endpoint-id] :as _request-map}
   {:keys [prometheus-registry service targets] :as _http-client}]
  (let [request-fn (method->request-fn method)
        uri (-> (get targets target) (str endpoint))
        async-callback-fn (fn [{:keys [opts] :as response}]
                            (future (when prometheus-registry
                                      (http-response-metric! service (:status response) endpoint-id prometheus-registry)
                                      (http-response-timing-metric! service endpoint-id (:start-ms opts) prometheus-registry)))
                            response)]
    (request-fn uri (assoc payload :start-ms (System/currentTimeMillis)) async-callback-fn)))

(s/defmethod request! :test
  [{:keys [method endpoint target payload] :as request-map}
   {:keys [requests targets] :as _http-client}]
  (let [uri (-> (get targets target) (str endpoint))
        request-fn (method->request-fn method)]
    (swap! requests conj request-map)
    (request-fn uri payload)))

(defn requests
  [{:keys [requests]}]
  (map (fn [request]
         (medley/update-existing-in request [:payload :body] #(json/decode % true))) @requests))

(defmethod ig/init-key ::http-client
  [_ {:keys [components]}]
  (log/info :starting ::http-client)
  (let [targets (-> components :config :targets)]
    (medley/assoc-some {:requests    (atom [])
                        :service     (-> components :config :service-name)
                        :current-env (-> components :config :current-env)
                        :targets     (s/validate models.targets/Targets targets)}
                       :prometheus-registry (-> components :prometheus :registry))))

(defmethod ig/halt-key! ::http-client
  [_ _]
  (log/info :stopping ::http-client))
