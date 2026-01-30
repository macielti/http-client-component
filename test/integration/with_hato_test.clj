(ns with-hato-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [http-client-component.with-hato :as component.http-client-with-hato]
            [integrant.core :as ig]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as s]))

(def system-setup
  {::component.http-client-with-hato/http-client {:components {:config {:service-name "integration-tests"
                                                                        :targets      {:example "https://example.com"
                                                                                       :echo    "https://echo.free.beeceptor.com"}
                                                                        :current-env  :prod}}}})

(s/deftest http-client-with-hato-remote-targets-test
  (let [system (ig/init system-setup)
        http-client (-> system ::component.http-client-with-hato/http-client)]

    (testing "That we can perform a GET HTTP Request"
      (is (match? {:status  200
                   :uri     "https://example.com/"
                   :version :http-2
                   :body    string?}
                  (component.http-client-with-hato/request! {:endpoint    "/"
                                                             :method      :get
                                                             :target      :example
                                                             :endpoint-id :get-http-request-example}
                                                            http-client))))

    (testing "That we can perform a GET HTTP Request passing headers"
      (let [response (component.http-client-with-hato/request! {:endpoint    "/"
                                                                :method      :get
                                                                :target      :echo
                                                                :endpoint-id :get-http-request-example-with-headers
                                                                :payload     {:headers {"User-Agent"    "Http Client Component With Hato"
                                                                                        "X-Test-Header" "Tested - OK"}}}
                                                               http-client)]
        (is (match? {:headers {:User-Agent    "Http Client Component With Hato"
                               :X-Test-Header "Tested - OK"}}
                    (json/decode (:body response) true)))))

    (testing "That we can perform a POST HTTP Request passing headers"
      (let [response (component.http-client-with-hato/request! {:endpoint    "/"
                                                                :method      :post
                                                                :target      :echo
                                                                :endpoint-id :post-http-request-example
                                                                :payload     {:headers {"User-Agent"    "Http Client Component With Hato"
                                                                                        "X-Test-Header" "Tested - OK"}
                                                                              :body    (json/encode {:test :ok})}}
                                                               http-client)]
        (is (match? {:headers {:User-Agent    "Http Client Component With Hato"
                               :X-Test-Header "Tested - OK"}
                     :rawBody "{\"test\":\"ok\"}"}
                    (json/decode (:body response) true)))))

    (ig/halt! system)))