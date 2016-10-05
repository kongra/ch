;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(ns kongra.ch)

;; PREDICATE (CLOJURE PROC.) CHeck

(defn chmsg
  [x]
  (with-out-str
    (print "Illegal value ")   (pr x)
    (print " of type ") (pr (class x))))

(defn- pred-call-form
  [form x]
  (let [form (if (symbol? form) (vector form) form)]
    (seq (conj (vec form) x))))

(defmacro ch {:style/indent 1}
  ([pred x]
   (let [x'   (gensym "x__")
         form (pred-call-form pred x')]
     `(let [~x' ~x] (assert ~form (chmsg ~x')) ~x')))

  ([pred #_ be-pred _ x]
   (let [form (pred-call-form pred x)]
     `(boolean ~form))))

;; CHECK GENERATOR

(defn- insert-noparam
  [params]
  (vec (concat (butlast params)
               (list '_)
               (when (seq params) (list (last params))))))

(defn- insert-noarg
  [[ccseq [cconcat & cclists]]]
  (assert (=  ccseq        `seq))
  (assert (=  cconcat   `concat))
  (assert (>= (count cclists) 2))
  (let [lsts   (butlast  cclists)
        lst    (last     cclists)
        noarg `(list        'nil)]
    `(seq (concat ~@lsts ~noarg ~lst))))

(defmacro defch {:style/indent 1}
  [chname args form]
  (assert (vector? args))
  (let [args+ (insert-noparam args)
        form+ (insert-noarg   form)]
    `(defmacro ~chname {:style/indent 1}
       (~args  ~form)
       (~args+ ~form+))))

;; CLASS MEMBERSHIP CHeck
(defch chC [c x] `(ch (instance? ~c) ~x))

;; UNIT (NIL) CHeck
(defch chUnit [x] `(ch nil? ~x))

;; NON-UNIT (NOT-NIL) CHeck
(defmacro not-nil? [x] `(not (nil?   ~x)))
(defch      chSome [x] `(ch not-nil? ~x))

;; OBJECT TYPE EQUALITY CHeck
(defmacro like [y x] `(identical? (class ~y) (class ~x)))
(defch  chLike [y x] `(ch (like ~y) ~x))
