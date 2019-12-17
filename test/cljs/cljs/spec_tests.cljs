(ns ^:figwheel-always cljs.spec-tests
  (:require
   ;; SPEC
   [cljs.spec.alpha                         :as  spec]
   [cljs.kongra.spec.alpha.macros :refer-macros [chSpec specInstr specCheck]]
   [cljs.spec.test.alpha          :refer-macros [instrument instrument-1]]
   [cljs.spec.test.alpha                 :refer [instrument-1*]]
   [clojure.test.check]
   [clojure.test.check.properties]

   [cljs.test :refer-macros [deftest is run-tests]]))

(spec/check-asserts true)
(println "cljs:  spec/*compile-asserts*" spec/*compile-asserts*)
(println "cljs: (spec/check-asserts?)"  (spec/check-asserts?))

;; TEST CLOJURE: PASS
(spec/def ::posInt pos-int?)

(spec/fdef foo
  :args (spec/cat :x ::posInt)
  :ret  ::posInt)

(defn- foo [x]
  (chSpec ::posInt
          (+ x 3)))

(specInstr `foo)
(specCheck `foo)

;; TEST CLOJURE: FAILURES
(spec/fdef goo
  :args (spec/cat :x ::posInt)
  :ret  ::posInt)

(defn- goo [x]
  (chSpec ::posInt
          (- x 3)))

(specInstr `goo)
(specCheck `goo) ;; Fails dumping to the js/console

(deftest chSpecTest
  ;; Fails with-profile uberjar cause there is no exception then
  (is (thrown? js/Error (goo 1))))

(enable-console-print!)
(run-tests)
