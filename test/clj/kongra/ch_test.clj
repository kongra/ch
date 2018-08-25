;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(ns kongra.ch-test
  (:require [clojure.test :refer :all]
            [kongra.ch :refer :all]))

(deftype X []) (defchC chX X)
(deftype Y []) (defchC chY Y)
(deftype Z []) (defchC chZ Z)

(defprotocol   Foo (foo [self]))
(extend-type X Foo (foo [self]))
(def      x (X.))
(defchP  chFoo Foo)

(defch chMaybeX      `(chMaybe  chX                       ))
(defch chEitherXUnit `(chEither chX chUnit                ))
(defch chEitherXY    `(chEither chX    chY                ))
(defch chXYZ         `(ch| [chX chY chZ]                  ))
(defch chMaybeLike1  `(chMaybe (chLike 1                 )))
(defch chEitherLC    `(chEither (chC Long) (chC Character)))
(defch chEitherLC'
  `(chEither (ch (instance? Long)) (ch (instance? Character))))

(defch chCompound1
  `(chEither
       (chMaybe  (chLike "aaa"))
       (chEither (chMaybe (ch (instance? Long)))
                 (chMaybe (ch (instance? Character))))))

(deftest ch-test
  (testing "(ch ...)"
    (is (thrown? AssertionError (ch (nil?)           1)))
    (is (nil?                   (ch (nil?)         nil)))
    (is (false?                 (ch (nil?) :asPred   1)))
    (is (true?                  (ch (nil?) :asPred nil))))

  (testing "(ch ...) with symbolic preds"
    (is (thrown? AssertionError (ch  nil?            1)))
    (is (nil?                   (ch  nil?          nil)))
    (is (false?                 (ch  nil?  :asPred   1)))
    (is (true?                  (ch  nil?  :asPred nil))))

  (testing "(chC ...)"
    (is (= ""                   (chC String          "")))
    (is (thrown? AssertionError (chC String           1)))
    (is (thrown? AssertionError (chC String         nil)))
    (is (true?                  (chC String :asPred  "")))
    (is (false?                 (chC String :asPred   1)))
    (is (false?                 (chC String :asPred nil))))

  (testing "(defchC ...)"
    (is                         (chX         (X.)))
    (is (thrown? AssertionError (chX           1)))
    (is (thrown? AssertionError (chX         nil)))
    (is (true?                  (chX :asPred (X.))))
    (is (false?                 (chX :asPred    1)))
    (is (false?                 (chX :asPred nil))))

  (testing "(chP ...)"
    (is (= x                    (chP Foo            x)))
    (is (thrown? AssertionError (chP Foo          (Y.))))
    (is (thrown? AssertionError (chP Foo            1)))
    (is (thrown? AssertionError (chP Foo          nil)))
    (is (true?                  (chP Foo :asPred    x)))
    (is (false?                 (chP Foo :asPred (Y.))))
    (is (false?                 (chP Foo :asPred    1)))
    (is (false?                 (chP Foo :asPred nil))))

  (testing "(defchP ...)"
    (is (= x                    (chFoo            x)))
    (is (thrown? AssertionError (chFoo          (Y.))))
    (is (thrown? AssertionError (chFoo            1)))
    (is (thrown? AssertionError (chFoo          nil)))
    (is (true?                  (chFoo :asPred    x)))
    (is (false?                 (chFoo :asPred (Y.))))
    (is (false?                 (chFoo :asPred    1)))
    (is (false?                 (chFoo :asPred  nil))))

  (testing "(chLike ...)"
    (is                         (chLike     1          2))
    (is (thrown? AssertionError (chLike     1     "aaa")))
    (is (thrown? AssertionError (chLike "aaa"         2)))
    (is (thrown? AssertionError (chLike     1       nil)))

    (is (true?                  (chLike   2/3 :asPred   3/4)))
    (is (false?                 (chLike     1 :asPred "aaa")))
    (is (false?                 (chLike "aaa" :asPred     2)))
    (is (false?                 (chLike     1 :asPred   nil))))

  (testing "(chUnit ...)"
    (is (nil?                   (chUnit     nil)))
    (is (thrown? AssertionError (chUnit       1)))

    (is (true?                  (chUnit :asPred nil)))
    (is (false?                 (chUnit :asPred ""))))

  (testing "(chSome ...)"
    (is                         (chSome        1))
    (is (thrown? AssertionError (chSome      nil)))

    (is (true?                  (chSome :asPred  "")))
    (is (false?                 (chSome :asPred nil))))

  (testing "(chMaybe ...)"
    (is (nil?                   (chMaybe chX         nil)))
    (is                         (chMaybe chX         (X.)))
    (is (thrown? AssertionError (chMaybe chX         (Y.))))
    (is (true?                  (chMaybe chX :asPred nil)))
    (is (true?                  (chMaybe chX :asPred (X.))))
    (is (false?                 (chMaybe chX :asPred (Y.))))

    (is (nil?                   (chMaybe chUnit nil)))
    (is (thrown? AssertionError (chMaybe chUnit (X.))))
    (is (thrown? AssertionError (chMaybe chUnit (Y.)))))

  (testing "(chMaybe ...) with (defch ...)"
    (is (nil?                   (chMaybeX          nil)))
    (is                         (chMaybeX          (X.)))
    (is (thrown? AssertionError (chMaybeX          (Y.))))
    (is (true?                  (chMaybeX :asPred  nil)))
    (is (true?                  (chMaybeX :asPred (X.))))
    (is (false?                 (chMaybeX :asPred (Y.)))))

  (testing "(chEither ...)"
    (is (nil?                   (chEither chX chUnit  nil)))
    (is                         (chEither chX chUnit  (X.)))
    (is (thrown? AssertionError (chEither chX chUnit  (Y.))))
    (is                         (chEither chX chY     (X.)))
    (is                         (chEither chX chY     (Y.)))
    (is (thrown? AssertionError (chEither chX chY    (Z.))))
    (is (thrown? AssertionError (chEither chX chY     nil)))

    (is (true?   (chEither chX chUnit :asPred  nil)))
    (is (true?   (chEither chX chUnit :asPred (X.))))
    (is (false?  (chEither chX chUnit :asPred (Y.))))
    (is (true?   (chEither chX chY    :asPred (X.))))
    (is (true?   (chEither chX chY    :asPred (Y.))))
    (is (false?  (chEither chX chY    :asPred (Z.))))
    (is (false?  (chEither chX chY    :asPred nil))))

  (testing "(chEither ...) with (defch ...)"
    (is (nil?                   (chEitherXUnit nil)))
    (is                         (chEitherXUnit (X.)))
    (is (thrown? AssertionError (chEitherXUnit (Y.))))
    (is                         (chEitherXY    (X.)))
    (is                         (chEitherXY    (Y.)))
    (is (thrown? AssertionError (chEitherXY    (Z.))))
    (is (thrown? AssertionError (chEitherXY    nil)))

    (is (true?   (chEitherXUnit :asPred  nil)))
    (is (true?   (chEitherXUnit :asPred (X.))))
    (is (false?  (chEitherXUnit :asPred (Y.))))
    (is (true?   (chEitherXY    :asPred (X.))))
    (is (true?   (chEitherXY    :asPred (Y.))))
    (is (false?  (chEitherXY    :asPred (Z.))))
    (is (false?  (chEitherXY    :asPred nil))))

  (testing "(ch| ...)"
    (is                         (ch| [chX chY chZ]   (X.)))
    (is                         (ch| [chX chY chZ]   (Y.)))
    (is                         (ch| [chX chY chZ]   (Z.)))
    (is (thrown? AssertionError (ch| [chX chY chZ]   nil)))
    (is (thrown? AssertionError (ch| [chX chY chZ] "aaa")))
    (is (thrown? AssertionError (ch| [chX chY chZ]     1)))

    (is (true?   (ch| [chX chY chZ] :asPred  (X.))))
    (is (true?   (ch| [chX chY chZ] :asPred  (Y.))))
    (is (true?   (ch| [chX chY chZ] :asPred  (Z.))))
    (is (false?  (ch| [chX chY chZ] :asPred   nil)))
    (is (false?  (ch| [chX chY chZ] :asPred "aaa")))
    (is (false?  (ch| [chX chY chZ] :asPred     1)))

    (is                         (ch| [chX chZ chY]   (X.)))
    (is                         (ch| [chX chZ chY]   (Y.)))
    (is                         (ch| [chX chZ chY]   (Z.)))
    (is (thrown? AssertionError (ch| [chX chZ chY]   nil)))
    (is (thrown? AssertionError (ch| [chX chZ chY] "aaa")))
    (is (thrown? AssertionError (ch| [chX chZ chY]     1)))

    (is                         (ch| [chY chX chZ]   (X.)))
    (is                         (ch| [chY chX chZ]   (Y.)))
    (is                         (ch| [chY chX chZ]   (Z.)))
    (is (thrown? AssertionError (ch| [chY chX chZ]   nil)))
    (is (thrown? AssertionError (ch| [chY chX chZ] "aaa")))
    (is (thrown? AssertionError (ch| [chY chX chZ]     1)))

    (is                         (ch| [chY chZ chX]   (X.)))
    (is                         (ch| [chY chZ chX]   (Y.)))
    (is                         (ch| [chY chZ chX]   (Z.)))
    (is (thrown? AssertionError (ch| [chY chZ chX]   nil)))
    (is (thrown? AssertionError (ch| [chY chZ chX] "aaa")))
    (is (thrown? AssertionError (ch| [chY chZ chX]     1)))

    (is                         (ch| [chZ chX chY]   (X.)))
    (is                         (ch| [chZ chX chY]   (Y.)))
    (is                         (ch| [chZ chX chY]   (Z.)))
    (is (thrown? AssertionError (ch| [chZ chX chY]   nil)))
    (is (thrown? AssertionError (ch| [chZ chX chY] "aaa")))
    (is (thrown? AssertionError (ch| [chZ chX chY]     1)))

    (is                         (ch| [chZ chY chX]   (X.)))
    (is                         (ch| [chZ chY chX]   (Y.)))
    (is                         (ch| [chZ chY chX]   (Z.)))
    (is (thrown? AssertionError (ch| [chZ chY chX]   nil)))
    (is (thrown? AssertionError (ch| [chZ chY chX] "aaa")))
    (is (thrown? AssertionError (ch| [chZ chY chX]    1))))

  (testing "(ch| ...) with (defch ...)"
    (is                         (chXYZ       (X.)))
    (is                         (chXYZ       (Y.)))
    (is                         (chXYZ       (Z.)))
    (is (thrown? AssertionError (chXYZ       nil)))
    (is (thrown? AssertionError (chXYZ     "aaa")))
    (is (thrown? AssertionError (chXYZ         1)))

    (is (true?                  (chXYZ :asPred  (X.))))
    (is (true?                  (chXYZ :asPred  (Y.))))
    (is (true?                  (chXYZ :asPred  (Z.))))
    (is (false?                 (chXYZ :asPred   nil)))
    (is (false?                 (chXYZ :asPred "aaa")))
    (is (false?                 (chXYZ :asPred   1))))

  (testing "(chMaybe (chLike ...))"
    (is                         (chMaybe (chLike  1)         2))
    (is (nil?                   (chMaybe (chLike  1)       nil)))
    (is (thrown? AssertionError (chMaybe (chLike  1)     "xyz")))
    (is                         (chMaybe (chLike "")     "xyz"))
    (is (nil?                   (chMaybe (chLike "")       nil)))
    (is (thrown? AssertionError (chMaybe (chLike "")         2)))

    (is (true?                  (chMaybe (chLike  1) :asPred     2)))
    (is (true?                  (chMaybe (chLike  1) :asPred   nil)))
    (is (false?                 (chMaybe (chLike  1) :asPred "xyz")))
    (is (true?                  (chMaybe (chLike "") :asPred "xyz")))
    (is (true?                  (chMaybe (chLike "") :asPred   nil)))
    (is (false?                 (chMaybe (chLike "") :asPred     2))))

  (testing "(chMaybeLike1 ...)"
    (is                         (chMaybeLike1             2))
    (is (nil?                   (chMaybeLike1           nil)))
    (is (thrown? AssertionError (chMaybeLike1         "xyz")))
    (is (true?                  (chMaybeLike1 :asPred     2)))
    (is (true?                  (chMaybeLike1 :asPred   nil)))
    (is (false?                 (chMaybeLike1 :asPred "xyz"))))

  (testing "(chEitherLC ...)"
    (is                         (chEitherLC     2))
    (is                         (chEitherLC    \c))
    (is (thrown? AssertionError (chEitherLC "xyz")))
    (is (thrown? AssertionError (chEitherLC   nil)))

    (is (true?                  (chEitherLC :asPred     2)))
    (is (true?                  (chEitherLC :asPred    \c)))
    (is (false?                 (chEitherLC :asPred "xyz")))
    (is (false?                 (chEitherLC :asPred   nil))))

  (testing "(chEitherLC' ...)"
    (is                         (chEitherLC'     2))
    (is                         (chEitherLC'    \c))
    (is (thrown? AssertionError (chEitherLC' "xyz")))
    (is (thrown? AssertionError (chEitherLC'   nil)))

    (is (true?                  (chEitherLC' :asPred     2)))
    (is (true?                  (chEitherLC' :asPred    \c)))
    (is (false?                 (chEitherLC' :asPred "xyz")))
    (is (false?                 (chEitherLC' :asPred   nil))))

  (testing "(chMaybe (ch (instance? ...)) ...)"
    (is                         (chMaybe (ch (instance? String))     "aaa"))
    (is (nil?                   (chMaybe (ch (instance? String))       nil)))
    (is (thrown? AssertionError (chMaybe (ch (instance? String))         1)))

    (is (true?                  (chMaybe (ch (instance? String)) :asPred "aaa")))
    (is (true?                  (chMaybe (ch (instance? String)) :asPred   nil)))
    (is (false?                 (chMaybe (ch (instance? String)) :asPred     1))))

  (testing "(chCompound1 ...)"
    (is                         (chCompound1 (+ 1 2 3 4)))
    (is                         (chCompound1          \c))
    (is                         (chCompound1       "xyz"))
    (is (nil?                   (chCompound1         nil)))
    (is (thrown? AssertionError (chCompound1         3/4)))

    (is (true?                  (chCompound1 :asPred (+ 1 2 3 4))))
    (is (true?                  (chCompound1 :asPred          \c)))
    (is (true?                  (chCompound1 :asPred       "xyz")))
    (is (true?                  (chCompound1 :asPred         nil)))
    (is (false?                 (chCompound1 :asPred         3/4))))

  (testing "(chPoslong)"
    (is                         (chPoslong      1))
    (is (thrown? AssertionError (chPoslong      0)))
    (is (thrown? AssertionError (chPoslong     -1)))

    (is (true?                  (chPoslong :asPred  1)))
    (is (false?                 (chPoslong :asPred  0)))
    (is (false?                 (chPoslong :asPred -1))))

  (testing "(chPosLong)"
    (is                         (chPosLong      1))
    (is (thrown? AssertionError (chPosLong      0)))
    (is (thrown? AssertionError (chPosLong     -1)))

    (is (true?                  (chPosLong :asPred  1)))
    (is (false?                 (chPosLong :asPred  0)))
    (is (false?                 (chPosLong :asPred -1))))

  (testing "(chNatlong)"
    (is                         (chNatlong      1))
    (is                         (chNatlong      0))
    (is (thrown? AssertionError (chNatlong     -1)))

    (is (true?                  (chNatlong :asPred  1)))
    (is (true?                  (chNatlong :asPred  0)))
    (is (false?                 (chNatlong :asPred -1))))

  (testing "(chNatLong)"
    (is                         (chNatLong      1))
    (is                         (chNatLong      0))
    (is (thrown? AssertionError (chNatLong     -1)))

    (is (true?                  (chNatLong :asPred  1)))
    (is (true?                  (chNatLong :asPred  0)))
    (is (false?                 (chNatLong :asPred -1))))

  (testing "(ch (long-in? start end) ...)"
    (is                         (ch (long-in? 1 10)  5))
    (is (thrown? AssertionError (ch (long-in? 1 10)  0)))
    (is (thrown? AssertionError (ch (long-in? 1 10) -1)))
    (is (thrown? AssertionError (ch (long-in? 1 10) 11)))

    (is (true?                  (ch (long-in? 1 10) :asPred  5)))
    (is (false?                 (ch (long-in? 1 10) :asPred  0)))
    (is (false?                 (ch (long-in? 1 10) :asPred -1)))
    (is (false?                 (ch (long-in? 1 10) :asPred 11))))

  (testing "(ch (Long-in? start end) ...)"
    (is                         (ch (Long-in? 1 10)  5))
    (is (thrown? AssertionError (ch (Long-in? 1 10)  0)))
    (is (thrown? AssertionError (ch (Long-in? 1 10) -1)))
    (is (thrown? AssertionError (ch (Long-in? 1 10) 11)))

    (is (true?                  (ch (Long-in? 1 10) :asPred  5)))
    (is (false?                 (ch (Long-in? 1 10) :asPred  0)))
    (is (false?                 (ch (Long-in? 1 10) :asPred -1)))
    (is (false?                 (ch (Long-in? 1 10) :asPred 11)))))

(time (run-tests))
