;; Copyright (c) Konrad Grzanek
;; Created 2019-12-12
(ns cljs.kongra.spec.alpha.macros
  #?(:clj (:require
           [clojure.spec.alpha
            :as spec])))

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chSpec {:style/indent 1}
     [spec & body]
     ;; Works like spec/assert but it does not depend on *compile-asserts* nor
     ;; spec/check-asserts?
     `(spec/assert* ~spec (do ~@body))))

#?(:clj
   (defmacro specInstr
     [s]
     (when spec/*compile-asserts*
       `(when (cljs.spec.alpha/check-asserts?)
          (cljs.spec.test.alpha/instrument ~s)))))

#?(:clj
   (defmacro specCheck
     ([s]
      `(specCheck ~s {}))

     ([s opts]
      (when spec/*compile-asserts*
        `(when (cljs.spec.alpha/check-asserts?)
           (print "specCheck" ~s "...")
           (let [result#
                 (-> ~s
                   (cljs.spec.test.alpha/check ~opts)
                   first
                   :clojure.spec.test.check/ret)]

             (if (= (:result result#) true)
               (println (:num-tests result#)
                 "calls in"
                 (:time-elapsed-ms result#) "msecs")

               (println result#))))))))
