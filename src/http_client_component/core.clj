(ns http-client-component.core
  (:require [clojure.tools.logging :as log]
            [clj-http.lite.client :as client]
            [iapetos.core :as prometheus]
            [medley.core :as medley]
            [cheshire.core :as json]
            [camel-snake-kebab.core :as camel-snake-kebab]
            [schema.core :as s]
            [integrant.core :as ig]))

(def method->request-fn
  {:post   client/post
   :get    client/get
   :put    client/put
   :delete client/delete})

(defmulti request!
  (fn [_ {:keys [current-env]}]
    current-env))

(s/defmethod request! :prod
             [{:keys [method url payload endpoint-id] :as _request-map}
              {:keys [prometheus-registry service] :as _http-client}]
             (try (let [request-fn (method->request-fn method)
                        http-response (request-fn url payload)]
                    (when prometheus-registry
                      (prometheus/inc prometheus-registry :http-request-response {:status   (:status http-response)
                                                                                  :service  service
                                                                                  :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}))
                    http-response)
                  (catch Exception ex
                    (when prometheus-registry
                      (prometheus/inc prometheus-registry :http-request-response {:status   (:status (ex-data ex))
                                                                                  :service  service
                                                                                  :endpoint (camel-snake-kebab/->snake_case_string endpoint-id)}))
                    (log/error ex)
                    (throw ex))))

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