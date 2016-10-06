;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(ns kongra.ch)

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

(defmacro defch {:style/indent 1}
  [chname args form]
  (assert (vector? args))
  (let [args+ (insert-noparam args)
        form+ (insert-noarg   form)]
    `(defmacro ~chname {:style/indent 1}
       (~args  ~form)
       (~args+ ~form+))))

;; CLASS MEMBERSHIP
(defch chC [c x] `(ch (instance? ~c) ~x))

;; UNIT (NIL)
(defch chUnit [x] `(ch nil? ~x))

;; NON-UNIT (NOT-NIL)
(defmacro chSome* [x] `(not (nil?   ~x)))
(defch    chSome  [x] `(ch chSome*  ~x))

;; OBJECT TYPE EQUALITY
(defmacro chLike* [y x] `(identical? (class ~y) (class ~x)))
(defch    chLike  [y x] `(ch (chLike* ~y) ~x))

;; PODUCT AND CO-PRODUCT (DISCRIMINATED UNION)
(defmacro ch*
  [op chs x]
  (assert (vector? chs) "Must be a chs vector in (ch| ...)")
  (assert (seq     chs) "(ch| ...) must contain some chs"  )
  `(~op ~@(map #(pred-call-form % nil x) chs)))

(defch ch& [chs x] `(ch (ch* and ~chs) ~x))
(defch ch| [chs x] `(ch (ch* or  ~chs) ~x))

(defch chEither [chl chr x] `(ch| [~chl  ~chr]    ~x))
(defch chMaybe  [ch      x] `(chEither chUnit ~ch ~x))

;; (defch chString [x] `(chC String ~x))
