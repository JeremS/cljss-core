(ns cljss.selectors.parent-test
  (:use cljss.selectors.parent
        cljss.selectors.protocols
        cljss.protocols
        midje.sweet))

(fact "The parent selector isn't neutral"
  (neutral? &) => falsey)

(fact "The parent selector is its own simplifaction"
  (simplify &) => &)