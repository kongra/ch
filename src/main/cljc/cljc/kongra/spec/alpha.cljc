;; Copyright (c) Konrad Grzanek
;; Created 2019-12-12
(ns cljc.kongra.spec.alpha
  #?(:clj (:require
           [clojure.spec.alpha
            :as spec]

           [clojure.spec.test.alpha
            :as spectest])))

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chSpec {:style/indent 1}
     [spec & body]
     (assert (seq body))
     (let [body (if (= 1 (count body))
                  (first body)
                  `(do ~@body))]

       ;; Works like spec/assert but depends only on *compile-asserts* and not
       ;; spec/check-asserts?
       (if spec/*compile-asserts*
         `(spec/assert* ~spec ~body)
         body))))

#?(:clj
   (defmacro specInstr
     [s]
     (when spec/*compile-asserts*
       `(when (spec/check-asserts?)
          (spectest/instrument ~s)))))

#?(:clj
   (defmacro specCheck
     [s]
     (when spec/*compile-asserts*
       `(when (spec/check-asserts?)
          (print "specCheck" ~s "... ")
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
