;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(defproject kongra/ch "0.1.8"
  :description "Low-cost dynamic type checks for ADTs"
  :url         "http://github.com/kongra/ch"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure          "1.9.0"]
                 [primitive-math               "0.1.6"]
                 [org.clojure/clojurescript "1.10.339"]]

  :plugins      [[lein-cljsbuild               "1.1.7"]
                 [lein-figwheel               "0.5.16"
                  :exclusions [org.clojure/clojure]]]

  :profiles     {:repl
                 {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]
                  :plugins      [[lein-nodisassemble      "0.1.3" ]
                                 [cider/cider-nrepl       "0.17.0"]]}}
  :aot          :all

  :source-paths ["src/clj" "test"]
  :test-paths   ["test"]

  :global-vars  {*warn-on-reflection* true
                 *assert*             true
                 *print-length*       500}

  :pedantic? :warn

  :jvm-opts     ["-Dclojure.compiler.direct-linking=true"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload            "kongra.ch/onJSreload"}
                        :compiler {:main                 kongra.ch
                                   :asset-path           "js/compiled/out"
                                   :output-to            "resources/public/js/compiled/ch.js"
                                   :output-dir           "resources/public/js/compiled/out"
                                   :source-map-timestamp true }}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to            "resources/public/js/compiled/ch.js"
                                   :main                  kongra.ch
                                   :optimizations         :advanced
                                   :pretty-print          false }}]})
