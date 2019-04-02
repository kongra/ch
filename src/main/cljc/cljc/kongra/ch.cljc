;; Copyright (c) Konrad Grzanek
;; Created     2016-10-05
;; Re-designed 2018-12-21
(ns cljc.kongra.ch
  (:require
   [clojure.set
    :refer [intersection difference]]

   [clojure.string
    :refer [blank?]]

   #?(:cljs [cljs.pprint
             :refer [pprint]])

   #?(:cljs [cljc.kongra.ch.macros
             :refer-macros [chP chReg chC]])))

#?(:clj (set! *warn-on-reflection* true))

;; ERROR REPORTING
(defn errMessage
  [x]
  #?(:clj (with-out-str
            (print "ch(eck) failed on\n") (pr        x)
            (print " of type ")           (pr (class x)))

     :cljs (str    "ch(eck) failed on\n" (with-out-str (pprint       x))
                   "of type "            (with-out-str (pprint (type x)))
                   "and typeof "         (.typeOf js/goog x))))

;; REGISTRY OF COMMON CHECKS
(def checksRegistry (atom {}))

(declare chBool chSeq)

(defn asPred
  [check x]
  (chBool
   (try
     (check x)                              true
     #?(:clj  (catch AssertionError       _ false))
     #?(:clj  (catch ClassCastException   _ false))
     #?(:clj  (catch NullPointerException _ false))
     #?(:cljs (catch js/Error             _ false)))))

(defn chs
  [x]
  (chSeq
   (->> @checksRegistry
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

;; CLJ MACROS
#?(:clj (defmacro chP
          [expr]
          (let [x (symbol "x")]
            `(fn [~x] (assert ~expr (cljc.kongra.ch/errMessage ~x)) ~x))))

#?(:clj (defmacro chC
          [expr]
          (let [x (symbol "x")]
            `(fn
               ([check#]
                (fn [~x]
                  (assert ~expr (cljc.kongra.ch/errMessage ~x))
                  (doseq [e# ~x] (check# e#))
                  ~x))

               ([check# ~x]
                (assert ~expr (cljc.kongra.ch/errMessage ~x))
                (doseq [e# ~x] (check# e#))
                ~x)))))

#?(:clj (defmacro chReg
          ([check]
           (let [name (str check)]
             `(let [name# (str ~name)]
                (assert (fn? ~check))
                (swap! cljc.kongra.ch/checksRegistry
                       (fn [m#]
                         (when (m# name#)
                           (println "WARNING: (ch)eck name already in use:" name#))
                         (assoc m# name# ~check)))
                nil)))

          ([name check]
           `(swap! cljc.kongra.ch/checksRegistry assoc ~name ~check))))

#?(:clj (defmacro defchP
          [name expr]
          (let [name (vary-meta name assoc
                                :arglists `'([~(symbol "x")])
                                :style/indent [0])]
            `(def ~name (chP ~expr)))))

#?(:clj (defmacro defchC
          [name expr]
          (let [name (vary-meta name assoc
                                :arglists `'([~(symbol "x")]
                                             [~(symbol "check") ~(symbol "x")])
                                :style/indent [0])]
            `(def ~name (chC ~expr)))))

;; COMMON CHECKS
(defn chOptional [check x] (if (nil? x) x (check x)))

(def chIdent (fn [x]     x))
(def chUnit  (chP (nil?  x)))
(def chSome  (chP (some? x)))

(def chAtom
  (chP (instance?
        #?(:clj  clojure.lang.Atom
           :cljs    cljs.core/Atom) x))) (chReg chAtom)

(def chAtomOf
  (chC (instance?
        #?(:clj  clojure.lang.Atom
           :cljs    cljs.core/Atom) x)))

(def chAssoc        (chP (associative? x))) (chReg      chAssoc)
(def chBool         (chP (boolean?     x))) (chReg       chBool)
(def chChar         (chP (char?        x))) (chReg       chChar)
(def chColl         (chP (coll?        x))) (chReg       chColl)
(def chCounted      (chP (counted?     x))) (chReg    chCounted)
(def chDelay        (chP (delay?       x))) (chReg      chDelay)
(def chFn           (chP (fn?          x))) (chReg         chFn)
(def chIfn          (chP (ifn?         x))) (chReg        chIfn)
(def chIndexed      (chP (indexed?     x))) (chReg    chIndexed)
(def chInteger      (chP (integer?     x))) (chReg    chInteger)
(def chKeyword      (chP (keyword?     x))) (chReg    chKeyword)
(def chList         (chP (list?        x))) (chReg       chList)
(def chMap          (chP (map?         x))) (chReg        chMap)
(def chNonEmpty     (chP (seq          x))) (chReg   chNonEmpty)
(def chRecord       (chP (record?      x))) (chReg     chRecord)
(def chReduced      (chP (reduced?     x))) (chReg    chReduced)
(def chReversible   (chP (reversible?  x))) (chReg chReversible)
(def chSeq          (chP (seq?         x))) (chReg        chSeq)
(def chSeqable      (chP (seqable?     x))) (chReg    chSeqable)
(def chSequential   (chP (sequential?  x))) (chReg chSequential)
(def chSet          (chP (set?         x))) (chReg        chSet)
(def chSorted       (chP (sorted?      x))) (chReg     chSorted)
(def chString       (chP (string?      x))) (chReg     chString)
(def chSymbol       (chP (symbol?      x))) (chReg     chSymbol)
(def chVar          (chP (var?         x))) (chReg        chVar)
(def chVector       (chP (vector?      x))) (chReg     chVector)

(def chAssocOf      (chC (associative? x)))
(def chCountedOf    (chC (counted?     x)))
(def chListOf       (chC (list?        x)))
(def chNonEmptyOf   (chC (seq          x)))
(def chReversibleOf (chC (reversible?  x)))
(def chSetOf        (chC (set?         x)))
(def chSortedOf     (chC (sorted?      x)))
(def chVectorOf     (chC (vector?      x)))

(def chNonBlank
  (chP #?(:clj  (not (blank? x))
          :cljs (and (string? x)
                     (not (.isEmptyOrWhitespace js/goog.string x))))))
(chReg chNonBlank)

;; COMMON NUMBERS CH(ECK)S
(def chPosInt    (chP (and (int?    x) (pos?  x)))) (chReg    chPosInt)
(def chNegInt    (chP (and (int?    x) (neg?  x)))) (chReg    chNegInt)
(def chNatInt    (chP (and (int?    x) (>= x  0)))) (chReg    chNatInt)
(def chEvenInt   (chP (and (int?    x) (even? x)))) (chReg   chEvenInt)
(def chOddInt    (chP (and (int?    x) (odd?  x)))) (chReg    chOddInt)

(def chFloat     (chP      (float?  x)))            (chReg     chFloat)
(def chInt       (chP      (int?    x)))            (chReg       chInt)
(def chNumber    (chP      (number? x)))            (chReg    chNumber)

;; CLOJURE CHECKS
#?(:clj (defchP chAgent (instance? clojure.lang.Agent x)))
#?(:clj (chReg  chAgent))

#?(:clj (defchP  chASeq (instance? clojure.lang.ASeq  x)))
#?(:clj (chReg   chASeq))

#?(:clj (defchP chDeref (instance? clojure.lang.IDeref x)))
#?(:clj (chReg  chDeref))

#?(:clj (defchP chLazy (instance? clojure.lang.LazySeq x)))
#?(:clj (chReg  chLazy))

#?(:clj (defchP chLookup (instance? clojure.lang.ILookup x)))
#?(:clj (chReg  chLookup))

#?(:clj (defchP chRef (instance? clojure.lang.Ref x)))
#?(:clj (chReg  chRef))

#?(:clj (def   chFuture (chP (future? x))))
#?(:clj (chReg chFuture))

#?(:clj (def   chClass (chP (class? x))))
#?(:clj (chReg chClass))

;; CLOJURE NUMBER CHECKS
#?(:clj (def   chRational (chP (rational? x))))
#?(:clj (chReg chRational))

#?(:clj (def   chRatio (chP (ratio? x))))
#?(:clj (chReg chRatio))

#?(:clj (def   chDecimal (chP (decimal? x))))
#?(:clj (chReg chDecimal))

#?(:clj (defn chLong ^long [^long x] x))
#?(:clj (chReg chLong))

#?(:clj (defn chNatLong ^long
          [^long x]
          (assert (>= x 0) (errMessage x))
          x))

#?(:clj (chReg chNatLong))

#?(:clj (defn chPosLong ^long
          [^long x]
          (assert (> x 0) (errMessage x))
          x))

#?(:clj (chReg chPosLong))

#?(:clj (defn chLongIn ^long
          [^long m ^long n ^long x]
          (assert (<= m n))
          (assert (<= m x) (errMessage x))
          (assert (<= x n) (errMessage x))
          x))

#?(:clj (defn  chDouble ^double [^double x] x))
#?(:clj (chReg chDouble))

#?(:clj (defn chDoubleIn ^double
          [^double a ^double b ^double x]
          (assert (<= a b))
          (assert (<= a x) (errMessage x))
          (assert (<= x b) (errMessage x))
          x))

#?(:clj (defn chPosDouble ^double
          [^double x]
          (assert (> x 0) (errMessage x))
          x))

#?(:clj (chReg chPosDouble))

#?(:clj (defn chNegDouble ^double
          [^double x]
          (assert (< x 0) (errMessage x))
          x))

#?(:clj (chReg chNegDouble))

#?(:clj (defn ch0+Double ^double
          [^double x]
          (assert (>= x 0) (errMessage x))
          x))

#?(:clj (chReg ch0+Double))

;; CLOJURE-SCRIPT CHECKS
#?(:cljs (def chObject    (chP      (object? x)))) #?(:cljs (chReg chObject))
#?(:cljs (def chArray     (chP      (array?  x)))) #?(:cljs (chReg  chArray))

#?(:cljs (def chDouble    (chP      (double? x))))            #?(:cljs (chReg    chDouble))
#?(:cljs (def chPosDouble (chP (and (double? x) (pos?  x))))) #?(:cljs (chReg chPosDouble))
#?(:cljs (def chNegDouble (chP (and (double? x) (neg?  x))))) #?(:cljs (chReg chNegDouble))
#?(:cljs (def ch0+Double  (chP (and (double? x) (>= x  0))))) #?(:cljs (chReg  ch0+Double))

;; JAVA CHECKS
(def chJavaColl (chP (instance? java.util.Collection x))) (chReg chJavaColl)
(def chJavaList (chP (instance? java.util.List       x))) (chReg chJavaList)
(def chJavaMap  (chP (instance? java.util.Map        x))) (chReg  chJavaMap)
(def chJavaSet  (chP (instance? java.util.Set        x))) (chReg  chJavaSet)
