;; Copyright (c) Konrad Grzanek
;; Created 2018-08-27
(ns cljs.kongra.ch.macros)

(defmacro chP
  [expr]
  (let [x (symbol "x")]
    (if *assert*
      `(fn [~x] (assert ~expr (cljs.kongra.ch/errMessage ~x)) ~x)
      `(fn [~x] ~x))))

(defmacro chC
  [expr]
  (let [x (symbol "x")]
    (if *assert*
      `(fn
        ([check#]
         (fn [~x]
           (assert ~expr (cljs.kongra.ch/errMessage ~x))
           (doseq [e# ~x] (check# e#))
           ~x))

        ([check# ~x]
         (assert ~expr (cljs.kongra.ch/errMessage ~x))
         (doseq [e# ~x] (check# e#))
         ~x))

      `(fn
         ([check#   ] (fn [~x] ~x))
         ([check# ~x] ~x)))))

(defmacro chD
  [expr]
  (let [x (symbol "x")]
    (if *assert*
      `(fn
         ([check#]
          (fn [~x]
            (assert ~expr (cljs.kongra.ch/errMessage ~x))
            (check# (deref ~x))
            ~x))

         ([check# ~x]
          (assert ~expr (cljs.kongra.ch/errMessage ~x))
          (check# (deref ~x))
          ~x))

      `(fn
         ([check#   ] (fn [~x] ~x))
         ([check# ~x] ~x)))))

(defmacro chReg
  ([check]
   (let [name (str check)]
     `(chReg ~name ~check)))

  ([name check]
   `(swap! cljs.kongra.ch/chsreg assoc ~name ~check)))
