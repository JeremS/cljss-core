(ns cljss.data.units-test
  (:require [units.core :as u])
  (:use cljss.data.units
        cljss.protocols
        midje.sweet))

(fact "We can compile lengths"
  (compile-as-property-value (u/px 10)) => "10px"
  (compile-as-property-value (u/em -50)) => "-50em"
  (compile-as-property-value (u/deg -50)) => "-50deg")