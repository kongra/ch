(ns spec-tests
  (:require
   [clojure.spec.alpha
    :as s]

   [malli.core
    :as m]

   [criterium.core
    :as cc]))

(println "clj: *assert*" *assert*)
(println "clj:  spec/*compile-asserts*" s/*compile-asserts*)
(println "clj: (spec/check-asserts?)"  (s/check-asserts?))

(let [spec (s/and int? (s/or :pos-int pos-int? :neg-int neg-int?))
      valid? (partial s/valid? spec)]
  (cc/quick-bench
    (valid? 0))) ;; 93 ns

(let [valid? (m/validator [:and int? [:or pos-int? neg-int?]])]
  (cc/quick-bench
    (valid? 1))) ;; 4 ns

;; 23 times

(let [spec (s/* (s/cat :prop string?,
                       :val (s/alt :s string?
                                   :b boolean?)))
      parse (partial s/conform spec)]
  (cc/quick-bench
    (parse ["-server" "foo" "-verbose" "-verbose" "-user" "joe"]))) ;; 49 us

(let [schema [:* [:catn
                  [:prop string?]
                  [:val [:altn
                         [:s string?]
                         [:b boolean?]]]]]
      parse (m/parser schema)]
  (cc/quick-bench
    (parse ["-server" "foo" "-verbose" "-verbose" "-user" "joe"]))) ;; 3 us

;; 16 times
