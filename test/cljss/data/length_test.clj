(ns cljss.data.length-test  
  (:refer-clojure :exclude [rem + - * /])
  (:require [midje.sweet :as m])
  (:use cljss.data.length
        clojure.algo.generic.arithmetic))


(m/fact "we can add two lengths"
        (+ (px 1) (px 3)) => (px 4)
        (+ (em 100) (em 10)) => (em 110))

(m/fact "we can negate a length"
        (- (em 100)) => (em -100)
        (- (em -100)) => (em 100))

(m/fact "we can substract two lengths"
        (- (px 1) (px 3)) => (px -2)
        (- (em 100) (em 10)) => (em 90))


(m/fact "we can multiply a length"
        (* (em 100) 10) => (em 1000)
        (* 10 (em 100)) => (em 1000))