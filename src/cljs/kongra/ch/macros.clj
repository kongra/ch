;; Copyright (c) Konrad Grzanek
;; Created 2018-08-27
(ns kongra.ch.macros)

(defmacro chP
  [expr]
  (let [x (symbol "x")]
    `(fn [~x]
       (assert ~expr (kongra.ch/errMessage ~x))
       ~x)))

(defmacro chC
  [expr]
  (let [x (symbol "x")]
    `(fn
       ;; FUNCTOR (CONTAINTER) ONLY CH(ECK)
       ([check#]
        (fn [~x]
          (assert ~expr (kongra.ch/errMessage ~x))
          (doseq [e# ~x] (check# e#))
          ~x))

       ;; FUNCTOR (CONTAINER) CH(ECK) + check# ON ITS ELEMENTS
       ([check# ~x]
        (assert ~expr (kongra.ch/errMessage ~x))
        (doseq [e# ~x] (check# e#))
        ~x))))

(defmacro chReg
  ([check]
   (let [name (str check)]
     `(chReg ~name ~check)))

  ([name check]
   `(swap! kongra.ch/chsreg assoc ~name ~check)))
