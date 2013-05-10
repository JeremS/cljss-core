(ns ^{:author "Jeremy Schoffen."}
  cljss.core-test
  (:require [cljss.parse :as parse]
            [cljss.precompilation :as pre])
  (:refer-clojure :exclude (rem))
  (:use cljss.core
        [midje.sweet :only (fact facts)]))

(fact "We can compile"
  (compressed-css [:a :a :a]
                  "b {b: b;}"
                  [:c :c :c]
                  (inline-css "d {d: d;}")
                  (css-comment "comment"))
  => "a {a: a;}b {b: b;}c {c: c;}d {d: d;}"

  (css-with-style
   (assoc (:compressed styles) :comments true)

   [:a :a :a]
   "b {b: b;}"
   [:c :c :c]
   (inline-css "d {d: d;}")
   (css-comment "comment"))
  => "a {a: a;}b {b: b;}c {c: c;}d {d: d;}/* comment */")

(css-with-style
   (assoc (:compressed styles) :comments true)

   [:a :a :a]
   "b {b: b;}"
   [:c :c :c]
   (inline-css "d {d: d;}")
   (css-comment "comment"))