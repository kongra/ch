;; Copyright (c) Konrad Grzanek
;; Created 2019-12-12
(ns ^:figwheel-always cljc.kongra.spec.alpha
  #?(:clj (:require
           [clojure.spec.alpha
            :as spec]

           [clojure.spec.test.alpha
            :as spectest]))

  #?(:cljs (:require
            [cljs.spec.alpha
             :as spec]

            [clojure.test.check]
            [clojure.test.check.properties]

            [cljs.spec.test.alpha
             :refer [instrument-1*]]))

  #?(:cljs (:require-macros
            [cljs.spec.test.alpha
             :refer [instrument instrument-1]]

            [cljs.kongra.spec.alpha.macros
             :refer [chSpec specInstr specCheck]])))


#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chSpec {:style/indent 1}
     [spec & body]
     (assert (seq body))
     (let [body (if (= 1 (count body))
                  (first body)
                  `(do ~@body))]

       (if spec/*compile-asserts*
         `(spec/assert* ~spec ~body)
         body))))

#?(:clj
   (defmacro specInstr
     [s]
     (when spec/*compile-asserts*
       `(spectest/instrument ~s))))

#?(:clj
   (defmacro specCheck
     [s]
     (when spec/*compile-asserts*
       `(do (print "specCheck" ~s "... ")
            (let [result#
                  (-> ~s
                      (spectest/check {:num-tests 1e4})
                      first
                      :clojure.spec.test.check/ret)]

              (if (= (:result result#) true)
                (println (:num-tests result#)
                         "calls in"
                         (:time-elapsed-ms result#) "msecs")

                (println result#)))))))

;; CLOJURE: DEBUG COMPILATION FLAGS
#?(:clj  (println ":clj *assert*"                        *assert*))
#?(:clj  (println ":clj *compile-asserts*" spec/*compile-asserts*))
#?(:clj
   (println ":clj clojure.compiler.direct-linking"
            (System/getProperty "clojure.compiler.direct-linking")))

;; ;; TEST CLOJURE: PASS
;; #?(:clj (spec/def ::posInt pos-int?))

;; #?(:clj (spec/fdef foo
;;           :args (spec/cat :x ::posInt)
;;           :ret  ::posInt))

;; #?(:clj (defn- foo [x]
;;           (chSpec ::posInt
;;             (+ x 3))))

;; #?(:clj (specInstr `foo))
;; #?(:clj (specCheck `foo))

;; ;; TEST CLOJURE: ERROR
;; #?(:clj (spec/fdef goo
;;           :args (spec/cat :x ::posInt)
;;           :ret  ::posInt))

;; #?(:clj (defn- goo [x]
;;           (chSpec ::posInt
;;             (- x 3))))

;; #?(:clj (specInstr `goo))
;; #?(:clj (specCheck `goo)) ;; Fails with ./nREPL.sh and lein compile
;; #?(:clj (goo 1))          ;; Fails with ./nREPL.sh and lein compile

;; ;; CLOJURE SCRIPT: DEBUG COMPILATION FLAGS
;; #?(:cljs  (println ":cljs *assert*"                        *assert*))
;; #?(:cljs  (println ":cljs *compile-asserts*" spec/*compile-asserts*))

;; ;; TEST CLOJURE SCRIPT: PASS
;; #?(:cljs (spec/def ::posInt pos-int?))

;; #?(:cljs (spec/fdef foo
;;            :args (spec/cat :x ::posInt)
;;            :ret  ::posInt))

;; #?(:cljs (defn- foo [x]
;;            (chSpec ::posInt
;;                    (+ x 3))))

;; #?(:cljs (specInstr `foo))
;; #?(:cljs (specCheck `foo))

;; ;; TEST CLOJURE SCRIPT: ERROR
;; #?(:cljs (spec/fdef goo
;;            :args (spec/cat :x ::posInt)
;;            :ret  ::posInt))

;; #?(:cljs (defn- goo [x]
;;            (chSpec ::posInt
;;              (- x 3))))

;; #?(:clj (specInstr `goo))
;; #?(:clj (specCheck `goo)) ;; Fails with ./fig.sh
;; #?(:clj (goo 1))          ;; Fails with ./fig.sh
