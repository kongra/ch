;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(defproject kongra/ch "0.1.16"
  :description "Low-cost dynamic type and constraints checks"
  :url         "http://github.com/kongra/ch"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure         "1.10.0"]
                 [primitive-math               "0.1.6"]
                 [org.clojure/clojurescript "1.10.520"]]

  :plugins      [[lein-cljsbuild "1.1.7"]]

  :aot          :all
  :source-paths ["src/main/clojure" "src/main/clojure-script"]
  :test-paths   ["test/clojure"]

  :global-vars  {*warn-on-reflection* false
                 *print-length*         500}

  ;; :pedantic? :warn

  :jvm-opts ["-Dclojure.compiler.direct-linking=true"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :aliases {"fig:repl" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :profiles {:repl {:plugins [[lein-nodisassemble "0.1.3"]
                              [cider/cider-nrepl "0.21.0"]]

                    :middleware [lein-nodisassemble.plugin/middleware
                                 cider-nrepl.plugin/middleware]}

             :dev  {:dependencies  [[com.bhauman/figwheel-main "0.2.0"]
                                    [com.bhauman/rebel-readline-cljs "0.1.4"]]

                    :source-paths   ["src/main/clojure-script"]
                    :resource-paths ["target"]}}
  :cljsbuild
  {:builds
   [{:id "min"
     :source-paths ["src/main/clojure-script"]
     :compiler {:output-to       "resources/public/js/compiled/ch.js"
                :main             cljs.kongra.ch
                :optimizations    :advanced
                :static-fns       true
                :fn-invoke-direct true
                :pretty-print     false
                :elide-asserts    true
                }}]}

  :figwheel { :css-dirs ["resources/public/css"]})
