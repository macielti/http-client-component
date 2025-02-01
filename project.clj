(defproject net.clojars.macielti/http-client-component "1.2.1"

  :description "HTTP Client Component"

  :url "https://github.com/macielti/http-client-component"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.12.0"]
                 [http-kit "2.8.0"]
                 [camel-snake-kebab "0.4.3"]
                 [integrant "0.13.1"]
                 [prismatic/schema "1.4.1"]
                 [clj-commons/iapetos "0.1.14"]
                 [org.clojure/tools.logging "1.3.0"]
                 [dev.weavejester/medley "1.8.1"]
                 [cheshire "5.13.0"]]

  :profiles {:dev {:test-paths   ^:replace ["test/unit" "test/integration" "test/helpers"]

                   :plugins      [[lein-cloverage "1.2.4"]
                                  [com.github.clojure-lsp/lein-clojure-lsp "1.4.16"]
                                  [com.github.liquidz/antq "RELEASE"]]

                   :dependencies [[hashp "0.2.2"]]

                   :injections   [(require 'hashp.core)]

                   :aliases      {"clean-ns"     ["clojure-lsp" "clean-ns" "--dry"] ;; check if namespaces are clean
                                  "format"       ["clojure-lsp" "format" "--dry"] ;; check if namespaces are formatted
                                  "diagnostics"  ["clojure-lsp" "diagnostics"]
                                  "lint"         ["do" ["clean-ns"] ["format"] ["diagnostics"]]
                                  "clean-ns-fix" ["clojure-lsp" "clean-ns"]
                                  "format-fix"   ["clojure-lsp" "format"]
                                  "lint-fix"     ["do" ["clean-ns-fix"] ["format-fix"]]}}}
  :resource-paths ["resources"])
