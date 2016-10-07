;; Copyright (c) Konrad Grzanek
;; Created 2016-10-05
(ns kongra.ch-test
  (:require [clojure.test :refer :all]
            [kongra.ch :refer :all]))

(deftype X []) (defchC chX X)
(deftype Y []) (defchC chY Y)
(deftype Z []) (defchC chZ Z)

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
    (is (thrown? AssertionError (ch (nil?)       1)))
    (is (nil?                   (ch (nil?)     nil)))
    (is (false?                 (ch (nil?) nil   1)))
    (is (true?                  (ch (nil?) nil nil))))

  (testing "(ch ...) with symbolic preds"
    (is (thrown? AssertionError (ch  nil?        1)))
    (is (nil?                   (ch  nil?      nil)))
    (is (false?                 (ch  nil?  nil   1)))
    (is (true?                  (ch  nil?  nil nil))))

  (testing "(chC ...)"
    (is (= ""                   (chC String      "")))
    (is (thrown? AssertionError (chC String       1)))
    (is (thrown? AssertionError (chC String     nil)))
    (is (true?                  (chC String nil  "")))
    (is (false?                 (chC String nil   1)))
    (is (false?                 (chC String nil nil))))

  (testing "(defchC ...)"
    (is                         (chX      (X.)))
    (is (thrown? AssertionError (chX        1)))
    (is (thrown? AssertionError (chX      nil)))
    (is (true?                  (chX nil (X.))))
    (is (false?                 (chX nil    1)))
    (is (false?                 (chX nil nil))))

  (testing "(chLike ...)"
    (is                         (chLike     1          2))
    (is (thrown? AssertionError (chLike     1     "aaa")))
    (is (thrown? AssertionError (chLike "aaa"         2)))
    (is (thrown? AssertionError (chLike     1       nil)))

    (is (true?                  (chLike   2/3 nil   3/4)))
    (is (false?                 (chLike     1 nil "aaa")))
    (is (false?                 (chLike "aaa" nil     2)))
    (is (false?                 (chLike     1 nil   nil))))

  (testing "(chUnit ...)"
    (is (nil?                   (chUnit     nil)))
    (is (thrown? AssertionError (chUnit       1)))

    (is (true?                  (chUnit nil nil)))
    (is (false?                 (chUnit nil ""))))

  (testing "(chSome ...)"
    (is                         (chSome        1))
    (is (thrown? AssertionError (chSome      nil)))

    (is (true?                  (chSome  nil  "")))
    (is (false?                 (chSome  nil nil))))

  (testing "(chMaybe ...)"
    (is (nil?                   (chMaybe chX      nil)))
    (is                         (chMaybe chX      (X.)))
    (is (thrown? AssertionError (chMaybe chX      (Y.))))
    (is (true?                  (chMaybe chX nil  nil)))
    (is (true?                  (chMaybe chX nil  (X.))))
    (is (false?                 (chMaybe chX nil  (Y.))))

    (is (nil?                   (chMaybe chUnit nil)))
    (is (thrown? AssertionError (chMaybe chUnit (X.))))
    (is (thrown? AssertionError (chMaybe chUnit (Y.)))))

  (testing "(chMaybe ...) with (defch ...)"
    (is (nil?                   (chMaybeX     nil)))
    (is                         (chMaybeX     (X.)))
    (is (thrown? AssertionError (chMaybeX     (Y.))))
    (is (true?                  (chMaybeX nil  nil)))
    (is (true?                  (chMaybeX nil (X.))))
    (is (false?                 (chMaybeX nil (Y.)))))

  (testing "(chEither ...)"
    (is (nil?                   (chEither chX chUnit  nil)))
    (is                         (chEither chX chUnit  (X.)))
    (is (thrown? AssertionError (chEither chX chUnit  (Y.))))
    (is                         (chEither chX chY     (X.)))
    (is                         (chEither chX chY     (Y.)))
    (is (thrown? AssertionError (chEither chX chY    (Z.))))
    (is (thrown? AssertionError (chEither chX chY     nil)))

    (is (true?   (chEither chX chUnit nil  nil)))
    (is (true?   (chEither chX chUnit nil (X.))))
    (is (false?  (chEither chX chUnit nil (Y.))))
    (is (true?   (chEither chX chY    nil (X.))))
    (is (true?   (chEither chX chY    nil (Y.))))
    (is (false?  (chEither chX chY    nil (Z.))))
    (is (false?  (chEither chX chY    nil nil))))

  (testing "(chEither ...) with (defch ...)"
    (is (nil?                   (chEitherXUnit nil)))
    (is                         (chEitherXUnit (X.)))
    (is (thrown? AssertionError (chEitherXUnit (Y.))))
    (is                         (chEitherXY    (X.)))
    (is                         (chEitherXY    (Y.)))
    (is (thrown? AssertionError (chEitherXY    (Z.))))
    (is (thrown? AssertionError (chEitherXY    nil)))

    (is (true?   (chEitherXUnit nil  nil)))
    (is (true?   (chEitherXUnit nil (X.))))
    (is (false?  (chEitherXUnit nil (Y.))))
    (is (true?   (chEitherXY    nil (X.))))
    (is (true?   (chEitherXY    nil (Y.))))
    (is (false?  (chEitherXY    nil (Z.))))
    (is (false?  (chEitherXY    nil nil))))

  (testing "(ch| ...)"
    (is                         (ch| [chX chY chZ]   (X.)))
    (is                         (ch| [chX chY chZ]   (Y.)))
    (is                         (ch| [chX chY chZ]   (Z.)))
    (is (thrown? AssertionError (ch| [chX chY chZ]   nil)))
    (is (thrown? AssertionError (ch| [chX chY chZ] "aaa")))
    (is (thrown? AssertionError (ch| [chX chY chZ]     1)))

    (is (true?   (ch| [chX chY chZ] #_ as-pred nil  (X.))))
    (is (true?   (ch| [chX chY chZ] #_ as-pred nil  (Y.))))
    (is (true?   (ch| [chX chY chZ] #_ as-pred nil  (Z.))))
    (is (false?  (ch| [chX chY chZ] #_ as-pred nil   nil)))
    (is (false?  (ch| [chX chY chZ] #_ as-pred nil "aaa")))
    (is (false?  (ch| [chX chY chZ] #_ as-pred nil     1)))

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

    (is (true?                  (chXYZ nil  (X.))))
    (is (true?                  (chXYZ nil  (Y.))))
    (is (true?                  (chXYZ nil  (Z.))))
    (is (false?                 (chXYZ nil   nil)))
    (is (false?                 (chXYZ nil "aaa")))
    (is (false?                 (chXYZ nil   1))))

  (testing "(chMaybe (chLike ...))"
    (is                         (chMaybe (chLike  1)         2))
    (is (nil?                   (chMaybe (chLike  1)       nil)))
    (is (thrown? AssertionError (chMaybe (chLike  1)     "xyz")))
    (is                         (chMaybe (chLike "")     "xyz"))
    (is (nil?                   (chMaybe (chLike "")       nil)))
    (is (thrown? AssertionError (chMaybe (chLike "")         2)))

    (is (true?                  (chMaybe (chLike  1) nil     2)))
    (is (true?                  (chMaybe (chLike  1) nil   nil)))
    (is (false?                 (chMaybe (chLike  1) nil "xyz")))
    (is (true?                  (chMaybe (chLike "") nil "xyz")))
    (is (true?                  (chMaybe (chLike "") nil   nil)))
    (is (false?                 (chMaybe (chLike "") nil     2))))

  (testing "(chMaybeLike1 ...)"
    (is                         (chMaybeLike1          2))
    (is (nil?                   (chMaybeLike1        nil)))
    (is (thrown? AssertionError (chMaybeLike1      "xyz")))
    (is (true?                  (chMaybeLike1  nil     2)))
    (is (true?                  (chMaybeLike1  nil   nil)))
    (is (false?                 (chMaybeLike1  nil "xyz"))))

  (testing "(chEitherLC ...)"
    (is                         (chEitherLC     2))
    (is                         (chEitherLC    \c))
    (is (thrown? AssertionError (chEitherLC "xyz")))
    (is (thrown? AssertionError (chEitherLC   nil)))

    (is (true?                  (chEitherLC nil     2)))
    (is (true?                  (chEitherLC nil    \c)))
    (is (false?                 (chEitherLC nil "xyz")))
    (is (false?                 (chEitherLC nil   nil))))

  (testing "(chEitherLC' ...)"
    (is                         (chEitherLC'     2))
    (is                         (chEitherLC'    \c))
    (is (thrown? AssertionError (chEitherLC' "xyz")))
    (is (thrown? AssertionError (chEitherLC'   nil)))

    (is (true?                  (chEitherLC' nil     2)))
    (is (true?                  (chEitherLC' nil    \c)))
    (is (false?                 (chEitherLC' nil "xyz")))
    (is (false?                 (chEitherLC' nil   nil))))

  (testing "(chMaybe (ch (instance? ...)) ...)"
    (is                         (chMaybe (ch (instance? String))     "aaa"))
    (is (nil?                   (chMaybe (ch (instance? String))       nil)))
    (is (thrown? AssertionError (chMaybe (ch (instance? String))         1)))

    (is (true?                  (chMaybe (ch (instance? String)) nil "aaa")))
    (is (true?                  (chMaybe (ch (instance? String)) nil   nil)))
    (is (false?                 (chMaybe (ch (instance? String)) nil     1))))

  (testing "(chCompound1 ...)"
    (is                         (chCompound1 (+ 1 2 3 4)))
    (is                         (chCompound1          \c))
    (is                         (chCompound1       "xyz"))
    (is (nil?                   (chCompound1         nil)))
    (is (thrown? AssertionError (chCompound1         3/4)))

    (is (true?                  (chCompound1 nil (+ 1 2 3 4))))
    (is (true?                  (chCompound1 nil          \c)))
    (is (true?                  (chCompound1 nil       "xyz")))
    (is (true?                  (chCompound1 nil         nil)))
    (is (false?                 (chCompound1 nil         3/4)))))

(time (run-tests))
