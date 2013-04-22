(ns cljss.selectors.parent-test
  (:require [midje.repl :as m])
  (:use cljss.selectors.parent
        cljss.protocols))

(m/fact "The parent selector isn't neutral"
        (neutral? &) => m/falsey)

(m/fact "The parent selector is its own simplifaction"
        (simplify &) => &)

(m/fact "The parent selector doesn't compile"
        (compile-as-selector &) => (m/throws Exception))