;; Copyright (c) Konrad Grzanek
;; Created 2018-08-27
(ns cljs.kongra.ch.macros)

#?(:clj (set! *warn-on-reflection* true))

#?(:clj
   (defmacro chP
     [expr]
     (let [x (symbol "x")]
       (if *assert*
         `(fn [~x] (assert ~expr (cljc.kongra.ch/errMessage ~x)) ~x)

         ;; Elided version
         `(fn [~x] ~x)))))

#?(:clj
   (defmacro chSpec
     [spec]
     (let [x (symbol "x")]
       (if *assert*
         `(fn [~x] (cljs.spec.alpha/assert ~spec ~x))

         ;; Elided version
         `(fn [~x] ~x)))))

#?(:clj
   (defmacro chC
     [expr]
     (let [x (symbol "x")]
       (if *assert*
         `(fn
            ([check#]
             (fn [~x]
               (assert ~expr (cljc.kongra.ch/errMessage ~x))
               (doseq [e# ~x] (check# e#))
               ~x))

            ([check# ~x]
             (assert ~expr (cljc.kongra.ch/errMessage ~x))
             (doseq [e# ~x] (check# e#))
             ~x))

         `(fn ;; Elided version
            ([check#   ] (fn [~x] ~x))
            ([check# ~x] ~x))))))

#?(:clj
   (defmacro chD
     [expr]
     (let [x (symbol "x")]
       (if *assert*
         `(fn
            ([check#]
             (fn [~x]
               (assert ~expr (cljc.kongra.ch/errMessage ~x))
               (check# (deref ~x))
               ~x))

            ([check# ~x]
             (assert ~expr (cljc.kongra.ch/errMessage ~x))
             (check# (deref ~x))
             ~x))

         `(fn ;; Elided version
            ([check#   ] (fn [~x] ~x))
            ([check# ~x] ~x))))))

#?(:clj
   (defmacro chReg
     ([check]
      (let [name (str check)]
        `(chReg ~name ~check)))

     ([name check]
      (when *assert* ;; Registering elided check makes no sense
        `(swap! cljc.kongra.ch/checksRegistry assoc ~name ~check)))))
