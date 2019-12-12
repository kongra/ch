;; Copyright (c) Konrad Grzanek
;; Created 2019-12-12
(ns cljc.kongra.spec.alpha)

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chSpec {:style/indent 1}
     [spec & body]
     (assert (seq body))
     (let [body (if (= 1 (count body))
                  (first body)
                  `(do ~@body))]

       (if clojure.spec.alpha/*compile-asserts*
         `(clojure.spec.alpha/assert* ~spec ~body)
         body))))

#?(:clj
   (defmacro specInstr
     [s]
     (when clojure.spec.alpha/*compile-asserts*
       `(clojure.spec.test.alpha/instrument ~s))))

#?(:clj
   (defmacro specCheck
     [s]
     (when clojure.spec.alpha/*compile-asserts*
       `(let [result#
              (-> ~s
                  (clojure.spec.test.alpha/check {:num-tests 1e4})
                  first
                  :clojure.spec.test.check/ret)]

          (if (= (:result result#) true)
            (println ~s
                     (str "x"   (:num-tests result#))
                     "in" (:time-elapsed-ms result#) "msecs")

            (println result#))))))
