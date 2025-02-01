(ns http-client-component.core
  (:require [camel-snake-kebab.core :as camel-snake-kebab]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [iapetos.core :as prometheus]
            [integrant.core :as ig]
            [medley.core :as medley]
            [org.httpkit.client :as hk-client]
            [schema.core :as s]))

(def method->request-fn
  {:post   hk-client/post
   :get    hk-client/get
   :put    hk-client/put
   :patch  hk-client/patch
   :delete hk-client/delete})

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
  [{:keys [method url payload endpoint-id] :as _request-map}
   {:keys [prometheus-registry service] :as _http-client}]
  (let [request-fn (method->request-fn method)
        async-callback-fn (fn [{:keys [opts] :as response}]
                            (future (when prometheus-registry
                                      (http-response-metric! service (:status response) endpoint-id prometheus-registry)
                                      (http-response-timing-metric! service endpoint-id (:start-ms opts) prometheus-registry)))
                            response)]
    (request-fn url (assoc payload :start-ms (System/currentTimeMillis)) async-callback-fn)))

(s/defmethod request! :test
  [{:keys [method url payload] :as request-map}
   {:keys [requests] :as _http-client}]
  (let [request-fn (method->request-fn method)]
    (swap! requests conj request-map)
    (request-fn url payload)))

(defn requests
  [{:keys [requests]}]
  (map (fn [request]
         (medley/update-existing-in request [:payload :body] #(json/decode % true))) @requests))

(defmethod ig/init-key ::http-client
  [_ {:keys [components]}]
  (log/info :starting ::http-client)
  (let [requests (atom [])]
    (medley/assoc-some {:requests    requests
                        :service     (-> components :config :service-name)
                        :current-env (-> components :config :current-env)}
                       :prometheus-registry (-> components :prometheus :registry))))

(defmethod ig/halt-key! ::http-client
  [_ _]
  (log/info :stopping ::http-client))
