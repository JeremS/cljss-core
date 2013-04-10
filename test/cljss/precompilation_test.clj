(ns cljss.precompilation-test
  (:require [midje.sweet :as m])
  (:use cljss.parse
        cljss.precompilation))

(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))


(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))

(m/fact "flatten rule flattens a rule and its sub rules..."
        (flatten-rule p-r) 
        => (list p-r1 p-r2 p-r3 p-r4))
