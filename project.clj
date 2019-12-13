;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(defproject kongra/ch "0.1.21"
  :description "Low-cost dynamic type and constraints checks"
  :url         "http://github.com/kongra/ch"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure         "1.10.0"]
                 [org.clojure/clojurescript "1.10.597"]]

  :plugins      [[lein-cljsbuild "1.1.7"]]

  :aot          :all
  :source-paths ["src/main/cljc" "src/main/clj"]
  :test-paths   ["test/clojure" ]

  :global-vars  {*warn-on-reflection* false
                 *assert*             false
                 *print-length*         500}

  ;; :pedantic? :warn

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :aliases {"fig:repl" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :profiles {:uberjar {:jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.spec.compile-asserts=false"]}

             :repl {:dependencies [[org.clojure/test.check "0.10.0"]]
                    :plugins      [[lein-nodisassemble      "0.1.3"]
                                   [cider/cider-nrepl      "0.22.3"]]

                    :middleware [lein-nodisassemble.plugin/middleware]

                    :global-vars {*assert* true}
                    :jvm-opts    ["-Dclojure.compiler.direct-linking=false"
                                  "-Dclojure.spec.compile-asserts=true"
                                  "-XX:-OmitStackTraceInFastThrow"
                                  "-server"
                                  "-Xms1g"
                                  "-Xmx1g"
                                  "-XX:+UseG1GC"
                                  "-XX:+UseStringDeduplication"
                                  "-XX:+DoEscapeAnalysis"
                                  "-XX:+UseCompressedOops"]}

             :dev  {:dependencies [[org.clojure/test.check         "0.10.0"]
                                   [com.bhauman/figwheel-main       "0.2.3"]
                                   [com.bhauman/rebel-readline-cljs "0.1.4"]]

                    :global-vars  {*assert* true}
                    :jvm-opts     ["-Dclojure.compiler.direct-linking=true"
                                   "-Dclojure.spec.compile-asserts=true"
                                   "-XX:-OmitStackTraceInFastThrow"
                                   "-server"
                                   "-Xms1g"
                                   "-Xmx1g"
                                   "-XX:+UseG1GC"
                                   "-XX:+UseStringDeduplication"
                                   "-XX:+DoEscapeAnalysis"
                                   "-XX:+UseCompressedOops"]

                    :source-paths   ^:replace ["src/main/cljc" "src/main/clj"]
                    :resource-paths ["target"]}}
  :cljsbuild
  {:builds
   [{:id "min"
     :source-paths ^:replace ["src/main/cljc" "src/main/clj"]
     :compiler {:output-to       "resources/public/js/compiled/ch.js"
                :main             cljc.kongra.ch
                :optimizations    :advanced
                :static-fns       true
                :fn-invoke-direct true
                :pretty-print     false
                :elide-asserts    true}}]})
