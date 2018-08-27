;; Copyright (c) Konrad Grzanek
;; Created 2018-08-27
(ns kongra.ch.macros)

(defmacro chP
  [expr]
  (let [x (symbol "x")]
    `(fn [~x]
       (assert ~expr (kongra.ch/errMessage ~x))
       ~x)))

(defmacro chReg
  [check]
  (let [name (str check)]
    `(swap! kongra.ch/chsreg assoc ~name ~check)))
