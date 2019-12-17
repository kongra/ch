(ns mytests
  (:use clojure.test)

  (:require
   [clojure.spec.alpha
    :as spec]

   [cljc.kongra.spec.alpha
    :refer :all]))

(println "clj: *assert*" *assert*)
(println "clj:  spec/*compile-asserts*" spec/*compile-asserts*)
(println "clj: (spec/check-asserts?)"  (spec/check-asserts?))

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

;; TEST CLOJURE: ERROR
(spec/fdef goo
  :args (spec/cat :x ::posInt)
  :ret  ::posInt)

(defn- goo [x]
  (chSpec ::posInt
          (- x 3)))

(specInstr `goo)
(specCheck `goo) ;; Fails dumping to the std. output

(deftest chSpecTest
  (is (thrown? Exception (goo 1))))
