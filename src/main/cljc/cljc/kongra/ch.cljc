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
             :refer-macros [chP chReg]])))

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

;; CHECKS
(defn chOptional
  [check x]
  (if (nil? x) x (check x)))

#?(:clj  (defchP chUnit      (nil?  x)))
#?(:cljs (def    chUnit (chP (nil?  x))))
#?(:clj  (defchP chSome      (some? x)))
#?(:cljs (def    chSome (chP (some? x))))

#?(:clj  (defchP chAgent (instance? clojure.lang.Agent x)))
#?(:clj  (chReg  chAgent))

#?(:clj  (defchP chAtom      (instance? clojure.lang.Atom x)))
#?(:cljs (def    chAtom (chP (instance? cljs.core/Atom    x))))
(chReg           chAtom)

#?(:clj (defchP  chASeq (instance? clojure.lang.ASeq  x)))
#?(:clj (chReg   chASeq))

#?(:clj  (defchP chBool (instance? Boolean x)))
#?(:cljs (def    chBool (chP (boolean?     x))))
(chReg           chBool)

;; (defchP chDeref      (instance? clojure.lang.IDeref     x)) (chReg      chDeref)
;; (defchP chIndexed    (instance? clojure.lang.Indexed    x)) (chReg    chIndexed)
;; (defchP chLazy       (instance? clojure.lang.LazySeq    x)) (chReg       chLazy)
;; (defchP chLookup     (instance? clojure.lang.ILookup    x)) (chReg     chLookup)
;; (defchP chRef        (instance? clojure.lang.Ref        x)) (chReg        chRef)
;; (defchP chSeqable    (instance? clojure.lang.Seqable    x)) (chReg    chSeqable)
;; (defchP chSequential (instance? clojure.lang.Sequential x)) (chReg chSequential)

;; (defchP chAssoc      (associative? x)) (chReg      chAssoc)
;; (defchP chChar       (char?        x)) (chReg       chChar)
;; (defchP chClass      (class?       x)) (chReg      chClass)
;; (defchP chColl       (coll?        x)) (chReg       chColl)
;; (defchP chCounted    (counted?     x)) (chReg    chCounted)
;; (defchP chDelay      (delay?       x)) (chReg      chDelay)
;; (defchP chFn         (fn?          x)) (chReg         chFn)
;; (defchP chFuture     (future?      x)) (chReg     chFuture)
;; (defchP chIfn        (ifn?         x)) (chReg        chIfn)
;; (defchP chInteger    (integer?     x)) (chReg    chInteger)
;; (defchP chKeyword    (keyword?     x)) (chReg    chKeyword)
;; (defchP chList       (list?        x)) (chReg       chList)
;; (defchP chMap        (map?         x)) (chReg        chMap)
;; (defchP chSet        (set?         x)) (chReg        chSet)
;; (defchP chRecord     (record?      x)) (chReg     chRecord)
;; (defchP chReduced    (reduced?     x)) (chReg    chReduced)
;; (defchP chReversible (reversible?  x)) (chReg chReversible)
;; (defchP chSeq        (seq?         x)) (chReg        chSeq)
;; (defchP chSorted     (sorted?      x)) (chReg     chSorted)
;; (defchP chString     (string?      x)) (chReg     chString)
;; (defchP chSymbol     (symbol?      x)) (chReg     chSymbol)
;; (defchP chVar        (var?         x)) (chReg        chVar)
;; (defchP chVector     (vector?      x)) (chReg     chVector)

;; (defchP chJavaColl   (instance? java.util.Collection x)) (chReg chJavaColl)
;; (defchP chJavaList   (instance? java.util.List       x)) (chReg chJavaList)
;; (defchP chJavaMap    (instance? java.util.Map        x)) (chReg  chJavaMap)
;; (defchP chJavaSet    (instance? java.util.Set        x)) (chReg  chJavaSet)

;; (defchP chNonBlank (not (blank? x))) (chReg chNonBlank)

;; ;; PRIMITIVE NUMBERS CH(ECK)S
;; (defn chLong ^long [^long x] x) (chReg chLong)

;; (defn chNatLong ^long
;;   [^long x]
;;   (assert (p/>= x 0) (errMessage x))
;;   x)

;; (chReg chNatLong)

;; (defn chPosLong ^long
;;   [^long x]
;;   (assert (p/> x 0) (errMessage x))
;;   x)

;; (chReg chPosLong)

;; (defn chLongIn ^long
;;   [^long m ^long n ^long x]
;;   (assert (<= m n))
;;   (assert (p/<= m x) (errMessage x))
;;   (assert (p/<= x n) (errMessage x))
;;   x)

;; (defn chDouble ^double [^double x] x) (chReg chDouble)

;; (defn chDoubleIn ^double
;;   [^double a ^double b ^double x]
;;   (assert (<= a b))
;;   (assert (p/<= a x) (errMessage x))
;;   (assert (p/<= x b) (errMessage x))
;;   x)

;; (defchP chFloat    (float?    x)) (chReg    chFloat)
;; (defchP chDecimal  (decimal?  x)) (chReg  chDecimal)
;; (defchP chNumber   (number?   x)) (chReg   chNumber)
;; (defchP chRatio    (ratio?    x)) (chReg    chRatio)
;; (defchP chRational (rational? x)) (chReg chRational)

;; CLOJURE SCRIPT
;; ;; ESSENTIAL CH(ECK)S
;; (def chIdent    (fn [x] x))
;; (def chString   (chP (string?  x))) (chReg chString)
;; (def chFn       (chP (fn?      x))) (chReg     chFn)
;; (def chIFn      (chP (ifn?     x))) (chReg    chIFn)

;; (def chInt      (chP (int?     x))) (chReg    chInt)
;; (def chDouble   (chP (double?  x))) (chReg chDouble)
;; (def chObject   (chP (object?  x))) (chReg chObject)
;; (def chArray    (chP (array?   x))) (chReg  chArray)

;; ;; NUMBERS CH(ECK)S
;; (def chPosInt   (chP (and (int?    x) (pos?  x)))) (chReg    chPosInt)
;; (def chNegInt   (chP (and (int?    x) (neg?  x)))) (chReg    chNegInt)
;; (def chNatInt   (chP (and (int?    x) (>= x  0)))) (chReg    chNatInt)
;; (def chEvenInt  (chP (and (int?    x) (even? x)))) (chReg   chEvenInt)
;; (def chOddInt   (chP (and (int?    x) (odd?  x)))) (chReg    chOddInt)

;; (def chPosDouble(chP (and (double? x) (pos? x))))  (chReg chPosDouble)
;; (def chNegDouble(chP (and (double? x) (neg? x))))  (chReg chNegDouble)
;; (def ch0+Double (chP (and (double? x) (>= x  0)))) (chReg  ch0+Double)

;; ;; CLJS CH(ECK)S
;; (def chColl          (chP (coll?        x)))  (chReg        chColl)
;; (def chList          (chP (list?        x)))  (chReg        chList)
;; (def chVector        (chP (vector?      x)))  (chReg      chVector)
;; (def chSet           (chP (set?         x)))  (chReg         chSet)
;; (def chSeq           (chP (seq?         x)))  (chReg         chSeq)

;; (def chListOf        (chC (list?        x)))
;; (def chVectorOf      (chC (vector?      x)))
;; (def chSetOf         (chC (set?         x)))

;; (def chNonEmpty      (chP (seq          x)))  (chReg    chNonEmpty)
;; (def chSequential    (chP (sequential?  x)))  (chReg  chSequential)
;; (def chAssociative   (chP (associative? x)))  (chReg chAssociative)
;; (def chSorted        (chP (sorted?      x)))  (chReg      chSorted)
;; (def chCounted       (chP (counted?     x)))  (chReg     chCounted)
;; (def chReversible    (chP (reversible?  x)))  (chReg  chReversible)

;; (def chNonEmptyOf    (chC (seq          x)))
;; (def chAssociativeOf (chC (associative? x)))
;; (def chSortedOf      (chC (sorted?      x)))
;; (def chCountedOf     (chC (counted?     x)))
;; (def chReversibleOf  (chC (reversible?  x)))

;; (def chMap (chP (map? x))) (chReg chMap)


;; (def chAtomOf (chC (instance? cljs.core/Atom x)))

;; (def chNonBlank
;;   (chP
;;    (and (string? x)
;;         (not (.isEmptyOrWhitespace js/goog.string x)))))
;; (chReg chNonBlank)
