(ns cljss.selectors.protocols)


(defprotocol Neutral
  (neutral? [this]
    "True if the selector is a neutral element in a composition of selectors."))

(defprotocol SimplifyAble
  (simplify [this]
    "Returns a simplyfied, equivalent version of a selector."))

(defprotocol Parent
  (parent? [this]
    "Detects if the parent selector is used.")
  (replace-parent [this replacement]
    "Replaces any apparition of the parent selecor it contains"))