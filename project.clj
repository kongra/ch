;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(defproject kongra/ch "0.1.10"
  :description "Low-cost dynamic type and constraints checks"
  :url         "http://github.com/kongra/ch"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure         "1.10.0"]
                 [primitive-math               "0.1.6"]
                 [org.clojure/clojurescript "1.10.439"
                  :exclusions [com.google.errorprone/error_prone_annotations
                               com.google.code.findbugs/jsr305]]

                 [reagent                      "0.8.1"]]

  :plugins      [[lein-cljsbuild               "1.1.7"]
                 [lein-figwheel               "0.5.16"
                  :exclusions [org.clojure/clojure]]]

  :profiles     {:repl
                 {:dependencies [[org.clojure/tools.nrepl "0.2.13"]]
                  :plugins      [[lein-nodisassemble      "0.1.3" ]
                                 [cider/cider-nrepl       "0.17.0"]]}}
  :aot          :all

  :source-paths ["src/main/clojure"]
  :test-paths   ["test/clojure"]

  :global-vars  {*warn-on-reflection* true
                 *print-length*       500}

  :pedantic? :warn

  :jvm-opts     ["-Dclojure.compiler.direct-linking=true"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["test/clojure/cljs" "src/main/clojure/cljs"]
                        :figwheel {:on-jsload            "cljs.kongra.ch-test/onJSreload"}
                        :compiler {:main                 cljs.kongra.ch-test
                                   :asset-path           "js/compiled/out"
                                   :output-to            "resources/public/js/compiled/ch.js"
                                   :output-dir           "resources/public/js/compiled/out"
                                   :source-map-timestamp true
                                   :pretty-print         true
                                   :optimize-constants   true
                                   }}
                       {:id "min"
                        :source-paths ["src/main/clojure/cljs"]
                        :compiler {:output-to            "resources/public/js/compiled/ch.js"
                                   :main                  cljs.kongra.ch
                                   :optimizations         :advanced
                                   :static-fns            true
                                   :fn-invoke-direct      true
                                   :pretty-print          false
                                   :elide-asserts         true
                                   }}]}

  :figwheel { :css-dirs ["resources/public/css"] })
