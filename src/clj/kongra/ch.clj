;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(ns kongra.ch
  (:require [clojure.set :as cset]))

;; PREDICATE (CLOJURE PROC.)
(defn chmsg
  [x]
  (with-out-str
    (print "Illegal value ")   (pr x)
    (print " of type ") (pr (class x))))

(defn- pred-call-form
  ([form x]
   (let [form (if (symbol? form) (vector form) form)]
     (seq (conj (vec form) x))))

  ([form _ x]
   (let [form (if (symbol? form) (vector form) form)]
     (concat form (list nil x)))))

(defmacro ch {:style/indent 1}
  ([pred x]
   (let [x'   (gensym "x__")
         form (pred-call-form pred x')]
     `(let [~x' ~x] (assert ~form (chmsg ~x')) ~x')))

  ([pred #_ be-pred _ x]
   (let [form (pred-call-form pred x)]
     `(boolean ~form))))

;; GENERATOR
(defn- insert-noparam
  [params]
  (vec (concat (butlast params)
               (list '_)
               (when (seq params) (list (last params))))))

(defn- insert-noarg
  [form]
  (let [;; lein eastwood passes a wrapper (sequence <form>), let's
        ;; strip it down:
        form (if (= (first form) `sequence) (second form) form)
        [ccseq [cconcat & cclists]] form]
    (assert (=  ccseq        `seq) (str "Illegal ccseq "     ccseq " in " form))
    (assert (=  cconcat   `concat) (str "Illegal cconcat " cconcat " in " form))
    (assert (>= (count cclists) 2) (str "Illegal cclists " cclists " in " form))
    (let [lsts   (butlast  cclists)
          lst    (last     cclists)
          noarg `(list        'nil)]
      `(seq (concat ~@lsts ~noarg ~lst)))))

(defn- append-arg
  [form x]
  (let [;; lein eastwood passes a wrapper (sequence <form>), let's
        ;; strip it down:
        form (if (= (first form) `sequence) (second form) form)
        [ccseq [cconcat & cclists]] form]
    (assert (=  ccseq        `seq) (str "Illegal ccseq "     ccseq " in " form))
    (assert (=  cconcat   `concat) (str "Illegal cconcat " cconcat " in " form))
    (assert (>= (count cclists) 2) (str "Illegal cclists " cclists " in " form))
    (let [arg `(list ~x)]
      `(seq (concat ~@cclists ~arg)))))

(defmacro defch {:style/indent 1}
  ([chname form]
   (let [x     (gensym "x__")
         form+ (append-arg form x) ]
     `(defch ~chname [~x] ~form+)))

  ([chname args form]
   (assert (vector? args))
   (let [args+ (insert-noparam args)
         form+ (insert-noarg   form)]
     `(defmacro ~chname {:style/indent 1}
        (~args  ~form)
        (~args+ ~form+)))))

;; CLASS MEMBERSHIP
(defch chC [c x] `(ch (instance? ~c) ~x))

(defmacro defchC
  [chname c]
  (let [x (gensym "x__")]
    `(defch ~chname [~x] `(chC ~~c ~~x))))

;; UNIT (NIL)
(defch chUnit [x] `(ch nil? ~x))

;; NON-UNIT (NOT-NIL)
(defn not-nil?
  {:inline (fn [x] `(if (nil? ~x) false true))}
               [x]  (if (nil?  x) false true))

(defch chSome [x] `(ch not-nil? ~x))

(defn foo [x]
  (chSome x))

;; OBJECT TYPE EQUALITY
(defmacro chLike* [y x] `(identical? (class ~y) (class ~x)))
(defch    chLike  [y x] `(ch (chLike* ~y) ~x))

;; PRODUCT AND CO-PRODUCT (DISCRIMINATED UNION)
(defmacro ch*
  [op chs x]
  (assert (vector? chs) "Must be a chs vector in (ch| ...)")
  (assert (seq     chs) "(ch| ...) must contain some chs"  )
  `(~op ~@(map #(pred-call-form % nil x) chs)))

(defch ch& [chs x] `(ch (ch* and ~chs) ~x))
(defch ch| [chs x] `(ch (ch* or  ~chs) ~x))

(defch chEither [chl chr x] `(ch| [~chl  ~chr]    ~x))
(defch chMaybe  [ch      x] `(chEither chUnit ~ch ~x))

;; CHS REGISTRY
(def ^:private CHS (atom {}))

(defn regch*
  [chname ch]
  (chUnit
   (do
     (assert (string? chname))
     (assert (fn?         ch))
     (swap! CHS
            (fn [m]
              (when (m chname)
                (println "WARNING: chname already in use:" chname))
              (assoc m chname ch))) nil)))

(defmacro regch
  [ch]
  (assert (symbol? ch))
  (let [x (gensym "x__")]
    `(regch* ~(str ch) (fn [~x] ~(pred-call-form ch nil x)))))

(defchC chSet clojure.lang.IPersistentSet) (regch chSet)

(defn chs
  ([]
   (chSet (apply sorted-set (sort (keys @CHS)))))

  ([x]
   (chSet (->> @CHS
               (filter (fn [[_ pred]] (pred x)))
               (map first)
               (apply sorted-set))))
  ([x & xs]
    (chSet (->> (cons x xs) (map chs) (apply cset/intersection)))))

(defn chdiffs
  [& xs]
  (chSet (->> xs (map chs) (apply cset/difference))))

;; COMMON CHS
(defchC chAgent           clojure.lang.Agent) (regch      chAgent)
(defchC chAtom             clojure.lang.Atom) (regch       chAtom)
(defchC chASeq             clojure.lang.ASeq) (regch       chASeq)
(defchC chBoolean                    Boolean) (regch    chBoolean)
(defchC chDeref          clojure.lang.IDeref) (regch      chDeref)
(defchC chDouble                      Double) (regch     chDouble)
(defchC chIndexed       clojure.lang.Indexed) (regch    chIndexed)
(defchC chLazy          clojure.lang.LazySeq) (regch       chLazy)
(defchC chLong                          Long) (regch       chLong)
(defchC chLookup        clojure.lang.ILookup) (regch     chLookup)
(defchC chRef               clojure.lang.Ref) (regch        chRef)
(defchC chSeqable       clojure.lang.Seqable) (regch    chSeqable)
(defchC chSequential clojure.lang.Sequential) (regch chSequential)

(defch  chAssoc           `(ch associative?)) (regch      chAssoc)
(defch  chChar                   `(ch char?)) (regch       chChar)
(defch  chClass                 `(ch class?)) (regch      chClass)
(defch  chColl                   `(ch coll?)) (regch       chColl)
(defch  chCounted             `(ch counted?)) (regch    chCounted)
(defch  chDecimal             `(ch decimal?)) (regch    chDecimal)
(defch  chDelay                 `(ch delay?)) (regch      chDelay)
(defch  chFloat                 `(ch float?)) (regch      chFloat)
(defch  chFn                       `(ch fn?)) (regch         chFn)
(defch  chFuture               `(ch future?)) (regch     chFuture)
(defch  chIfn                     `(ch ifn?)) (regch        chIfn)
(defch  chInteger             `(ch integer?)) (regch    chInteger)
(defch  chKeyword             `(ch keyword?)) (regch    chKeyword)
(defch  chList                   `(ch list?)) (regch       chList)
(defch  chMap                     `(ch map?)) (regch        chMap)
(defch  chNumber               `(ch number?)) (regch     chNumber)
(defch  chRatio                 `(ch ratio?)) (regch      chRatio)
(defch  chRational           `(ch rational?)) (regch   chRational)
(defch  chRecord               `(ch record?)) (regch     chRecord)
(defch  chReduced             `(ch reduced?)) (regch    chReduced)
(defch  chReversible       `(ch reversible?)) (regch chReversible)
(defch  chSeq                     `(ch seq?)) (regch        chSeq)
(defch  chSorted               `(ch sorted?)) (regch     chSorted)
(defch  chString               `(ch string?)) (regch     chString)
(defch  chSymbol               `(ch symbol?)) (regch     chSymbol)
(defch  chVar                     `(ch var?)) (regch        chVar)
(defch  chVec                  `(ch vector?)) (regch        chVec)

(defchC chJavaColl      java.util.Collection) (regch   chJavaColl)
(defchC chJavaList            java.util.List) (regch   chJavaList)
(defchC chJavaMap              java.util.Map) (regch    chJavaMap)
(defchC chJavaSet              java.util.Set) (regch    chJavaSet)
