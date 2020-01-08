;; Copyright (c) Konrad Grzanek
;; Created 2019-12-12
(ns cljs.kongra.spec.alpha.macros
  #?(:clj (:require
           [clojure.string     :as string]
           [clojure.walk       :as   walk]
           [clojure.spec.alpha :as   spec])))

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chSpec {:style/indent 1}
     [spec & body]
     ;; Works like spec/assert but it does not depend on *compile-asserts* nor
     ;; spec/check-asserts?
     `(cljs.spec.alpha/assert* ~spec (do ~@body))))

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
      (let [opts
            ;; The opts may be an integral meaning the :num-tests
            (if (pos-int? opts)
              {:clojure.spec.test.check/opts
               {:num-tests opts}}

              opts)]

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

                 (println result#)))))))))

;; NS/KEYS ALIASING PROBLEM SOLUTION
#?(:clj (defonce ^:private specAlAtom (atom {})))

#?(:clj
   (defn specAl*
     [form]
     (if (keyword? form)
       (let [formNs   (str (namespace form))
             formName (str (name      form))]

         (if-let [formNs1 (get @specAlAtom formNs)]
           (keyword formNs1 formName)

           ;; No al(iased) namespace, no transform
           form))

       ;; Not a keyword, nothing to transform
       form)))

#?(:clj
   (defmacro specAl
     [& body]
     (let [body (walk/postwalk specAl* body)]
       `(do ~@body))))

#?(:clj
   (spec/def ::nonBlank
     (spec/and string? (complement string/blank?))))

#?(:clj
   (defmacro specAl!
     [al ns]
     (spec/assert ::nonBlank al)
     (spec/assert ::nonBlank ns)
     (swap! specAlAtom assoc al ns)))

#?(:clj
   (defmacro specAlReset!
     []
     (reset! specAlAtom {})))
