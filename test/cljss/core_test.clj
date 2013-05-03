(ns ^{:author "Jeremy Schoffen."}
  cljss.core-test

  (:require [cljss.parse :as parse]
            [cljss.precompilation :as pre])


  (:refer-clojure :exclude (rem))
  (:use cljss.core
        [midje.sweet :only (fact facts)]


        clojure.tools.trace))

(fact "We can compile"
  (compressed-css [:a :a :a]
                  "b {b: b;}"
                  [:c :c :c]) => "a {a: a;}b {b: b;}c {c: c;}")