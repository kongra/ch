;; Copyright (c) Konrad Grzanek
;; Created 2018-08-25
(ns ^:figwheel-always kongra.ch
  (:require        [clojure.string   :refer [blank?       ]]
                   [clojure.set      :refer [intersection
                                             difference   ]])
  (:require-macros [kongra.ch.macros :refer [chP chC chReg]]))

;; REGISTRY
(def chsreg (atom {}))

;; ERROR REPORTING
(defn errMessage
  [x]
  (str "ch(eck) failed on\n" (with-out-str (cljs.pprint/pprint       x))
       "of type "            (with-out-str (cljs.pprint/pprint (type x)))
       "and typeof "         (-> js/goog   (.typeOf                  x))))

;; ESSENTIAL CH(ECK)S
(def chIdent    (fn [x] x))
(def chString   (chP (string?  x))) (chReg chString)
(def chUnit     (chP (nil?     x))) (chReg   chUnit)
(def chSome     (chP (some?    x))) (chReg   chSome)
(def chFn       (chP (fn?      x))) (chReg     chFn)
(def chIFn      (chP (ifn?     x))) (chReg    chIFn)
(def chBool     (chP (boolean? x))) (chReg   chBool)
(def chInt      (chP (int?     x))) (chReg    chInt)
(def chDouble   (chP (double?  x))) (chReg chDouble)
(def chObject   (chP (object?  x))) (chReg chObject)
(def chArray    (chP (array?   x))) (chReg  chArray)

;; NUMBERS CH(ECK)S
(def chPosInt   (chP (and (int?    x) (>  x  0)))) (chReg    chPosInt)
(def chNegInt   (chP (and (int?    x) (<  x  0)))) (chReg    chNegInt)
(def chNatInt   (chP (and (int?    x) (>= x  0)))) (chReg    chNatInt)
(def chEvenInt  (chP (and (int?    x) (even? x)))) (chReg   chEvenInt)
(def chOddInt   (chP (and (int?    x) (odd?  x)))) (chReg    chOddInt)

(def chPosDouble(chP (and (double? x) (>  x  0)))) (chReg chPosDouble)
(def chNegDouble(chP (and (double? x) (<  x  0)))) (chReg chNegDouble)
(def ch0+Double (chP (and (double? x) (>= x  0)))) (chReg  ch0+Double)

;; CLJS CH(ECK)S
(def chColl        (chC (coll?        x)))  (chReg "chColl"        (chColl        chIdent))
(def chList        (chC (list?        x)))  (chReg "chList"        (chList        chIdent))
(def chVector      (chC (vector?      x)))  (chReg "chVector"      (chVector      chIdent))
(def chSet         (chC (set?         x)))  (chReg "chSet"         (chSet         chIdent))
(def chSeq         (chC (seq?         x)))  (chReg "chSeq"         (chSeq         chIdent))

(def chNonEmpty    (chC (not (empty?  x)))) (chReg "chNonEmpty"    (chNonEmpty    chIdent))
(def chSequential  (chC (sequential?  x)))  (chReg "chSequential"  (chSequential  chIdent))
(def chAssociative (chC (associative? x)))  (chReg "chAssociative" (chAssociative chIdent))
(def chSorted      (chC (sorted?      x)))  (chReg "chSorted"      (chSorted      chIdent))
(def chCounted     (chC (counted?     x)))  (chReg "chCounted"     (chCounted     chIdent))
(def chReversible  (chC (reversible?  x)))  (chReg "chReversible"  (chReversible  chIdent))

(def chMap
  (chP (map? x)))
(chReg  chMap)

(def chAtom
  (chP (instance? cljs.core/Atom x)))
(chReg   chAtom)

(def chNonBlank
  (chP
   (and (string? x)
        (not (.isEmptyOrWhitespace js/goog.string x)))))
(chReg chNonBlank)

;; ADTs CH(ECK)S
(defn chMaybe [check x] (if (nil? x) x (check x)))

;; REGISTRY QUERYING
(defn asPred
  [check x]
  (chBool
   (try
     (check x)          true
     (catch js/Error _ false))))

(defn chs
  [x]
  (chSeq chString
         (->> @chsreg
              (map (fn [[name check]] (when (asPred check x) name)))
              (filter some?)
              sort)))

(defn chsAll
  [& xs]
  (chSeq chString
         (->> xs
              (map #(set (chs %)))
              (reduce intersection)
              sort)))

(defn chsDiff
  [& xs]
  (chSeq chString
         (->> xs
              (map #(set (chs %)))
              (reduce difference)
              sort)))
