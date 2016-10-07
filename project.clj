;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(defproject kongra/ch "0.1.0"
  :description "Low-cost dynamic type checks for ADTs"
  :url         "http://github.com/kongra/ch"
  :license     {:name "Eclipse Public License"
                :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure                  "1.8.0"]]
  :profiles     {:repl {:plugins [[lein-nodisassemble  "0.1.3"]]}}

  :aot          :all

  :source-paths ["src/clj" "test"]
  :test-paths   ["test"]

  :global-vars  {*warn-on-reflection* true
                 *assert*             true
                 *print-length*       500}

  :pedantic? :warn)
