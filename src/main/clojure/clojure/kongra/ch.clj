;; Copyright (c) Konrad Grzanek
;; Created     2016-10-05
;; Re-designed 2018-12-21
(ns clojure.kongra.ch
  (:require
   [clojure.set
    :refer [intersection difference]]

   [clojure.string
    :refer [blank?]]

   [primitive-math :as p]))

(set! *warn-on-reflection* true)

;; CH(ECK)S
(defn errMessage
  [x]
  (with-out-str
    (print "ch(eck) failed on\n") (pr        x)
    (print " of type ")           (pr (class x))))

(defmacro chP
  [expr]
  (let [x (symbol "x")]
    `(fn [~x] (assert ~expr (clojure.kongra.ch/errMessage ~x)) ~x)))

(defmacro chC
  [expr]
  (let [x (symbol "x")]
    `(fn
       ([check#]
        (fn [~x]
          (assert ~expr (cljs.kongra.ch/errMessage ~x))
          (doseq [e# ~x] (check# e#))
          ~x))

       ([check# ~x]
        (assert ~expr (cljs.kongra.ch/errMessage ~x))
        (doseq [e# ~x] (check# e#))
        ~x))))

(defmacro defchP
  [name expr]
  (let [name (vary-meta name assoc
                        :arglists `'([~(symbol "x")])
                        :style/indent [0])]
    `(def ~name (chP ~expr))))

(defmacro defchC
  [name expr]
  (let [name (vary-meta name assoc
                        :arglists `'([~(symbol "x")]
                                     [~(symbol "check") ~(symbol "x")])
                        :style/indent [0])]
    `(def ~name (chC ~expr))))

(defn chOptional [check x] (if (nil? x) x (check x)))

(defchP chUnit (nil?  x))
(defchP chSome (some? x))

;; REGISTRY
(def ^:private chsreg (atom {}))

(defmacro chReg
  ([check]
   (let [name (str check)]
     `(chReg ~name ~check)))

  ([name check]
   `(let [name# (str ~name)]
      (assert (fn? ~check))
      (swap! chsreg
             (fn [m#]
               (when (m# name#)
                 (println "WARNING: (ch)eck name already in use:" name#))
               (assoc m# name# ~check)))
      nil)))

;; COMMON CH(ECK)S
(defchP chAgent      (instance? clojure.lang.Agent      x)) (chReg      chAgent)
(defchP chAtom       (instance? clojure.lang.Atom       x)) (chReg       chAtom)
(defchP chASeq       (instance? clojure.lang.ASeq       x)) (chReg       chASeq)
(defchP chBool       (instance? Boolean                 x)) (chReg       chBool)
(defchP chDeref      (instance? clojure.lang.IDeref     x)) (chReg      chDeref)
(defchP chIndexed    (instance? clojure.lang.Indexed    x)) (chReg    chIndexed)
(defchP chLazy       (instance? clojure.lang.LazySeq    x)) (chReg       chLazy)
(defchP chLookup     (instance? clojure.lang.ILookup    x)) (chReg     chLookup)
(defchP chRef        (instance? clojure.lang.Ref        x)) (chReg        chRef)
(defchP chSeqable    (instance? clojure.lang.Seqable    x)) (chReg    chSeqable)
(defchP chSequential (instance? clojure.lang.Sequential x)) (chReg chSequential)

(defchP chAssoc      (associative? x)) (chReg      chAssoc)
(defchP chChar       (char?        x)) (chReg       chChar)
(defchP chClass      (class?       x)) (chReg      chClass)
(defchP chColl       (coll?        x)) (chReg       chColl)
(defchP chCounted    (counted?     x)) (chReg    chCounted)
(defchP chDelay      (delay?       x)) (chReg      chDelay)
(defchP chFn         (fn?          x)) (chReg         chFn)
(defchP chFuture     (future?      x)) (chReg     chFuture)
(defchP chIfn        (ifn?         x)) (chReg        chIfn)
(defchP chInteger    (integer?     x)) (chReg    chInteger)
(defchP chKeyword    (keyword?     x)) (chReg    chKeyword)
(defchP chList       (list?        x)) (chReg       chList)
(defchP chMap        (map?         x)) (chReg        chMap)
(defchP chSet        (set?         x)) (chReg        chSet)
(defchP chRecord     (record?      x)) (chReg     chRecord)
(defchP chReduced    (reduced?     x)) (chReg    chReduced)
(defchP chReversible (reversible?  x)) (chReg chReversible)
(defchP chSeq        (seq?         x)) (chReg        chSeq)
(defchP chSorted     (sorted?      x)) (chReg     chSorted)
(defchP chString     (string?      x)) (chReg     chString)
(defchP chSymbol     (symbol?      x)) (chReg     chSymbol)
(defchP chVar        (var?         x)) (chReg        chVar)
(defchP chVector     (vector?      x)) (chReg     chVector)

(defchP chJavaColl   (instance? java.util.Collection x)) (chReg chJavaColl)
(defchP chJavaList   (instance? java.util.List       x)) (chReg chJavaList)
(defchP chJavaMap    (instance? java.util.Map        x)) (chReg  chJavaMap)
(defchP chJavaSet    (instance? java.util.Set        x)) (chReg  chJavaSet)

(defchP chNonBlank (not (blank? x))) (chReg chNonBlank)

;; PRIMITIVE NUMBERS CH(ECK)S
(defn chLong ^long [^long x] x) (chReg chLong)

(defn chNatLong ^long
  [^long x]
  (assert (p/>= x 0) (errMessage x))
  x)

(chReg chNatLong)

(defn chPosLong ^long
  [^long x]
  (assert (p/> x 0) (errMessage x))
  x)

(chReg chPosLong)

(defn chLongIn ^long
  [^long m ^long n ^long x]
  (assert (<= m n))
  (assert (p/<= m x) (errMessage x))
  (assert (p/<= x n) (errMessage x))
  x)

(defn chDouble ^double [^double x] x) (chReg chDouble)

(defn chDoubleIn ^double
  [^double a ^double b ^double x]
  (assert (<= a b))
  (assert (p/<= a x) (errMessage x))
  (assert (p/<= x b) (errMessage x))
  x)

(defchP chFloat    (float?    x)) (chReg    chFloat)
(defchP chDecimal  (decimal?  x)) (chReg  chDecimal)
(defchP chNumber   (number?   x)) (chReg   chNumber)
(defchP chRatio    (ratio?    x)) (chReg    chRatio)
(defchP chRational (rational? x)) (chReg chRational)

;; REGISTRY QUERYING
(defn asPred
  [check x]
  (chBool
   (try
     (check x)                     true
     (catch AssertionError       _ false)
     (catch ClassCastException   _ false)
     (catch NullPointerException _ false))))

(defn chs
  [x]
  (chSeq
   (->> @chsreg
        (map (fn [[name check]] (when (asPred check x) name)))
        (filter some?)
        sort)))

(defn chsAll
  [& xs]
  (chSeq
   (->> xs
        (map #(set (chs %)))
        (reduce intersection)
        sort)))

(defn chsDiff
  [& xs]
  (chSeq
   (->> xs
        (map #(set (chs %)))
        (reduce difference)
        sort)))
