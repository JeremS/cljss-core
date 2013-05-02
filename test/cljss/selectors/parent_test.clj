(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.parent-test
  (:use cljss.selectors.parent
        cljss.protocols
        midje.sweet))

(fact "The parent selector isn't neutral"
  (neutral? &) => falsey)

(fact "The parent selector is its own simplifaction"
  (simplify &) => &)

(fact "The parent selector doesn't compile"
  (compile-as-selector &) => (throws Exception))