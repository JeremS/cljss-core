(ns cljss.selectors.protocols)

(defprotocol Neutral
  (neutral? [this] 
    "True if the selector is a neutral element in a composition of selectors."))

(defprotocol SimplifyAble
  (simplify [this]
    "Return a simplyfied, equivalent version of a selector."))

(defprotocol Parent
  (parent? [this]
    "Detects if the parent selector is used.")
  (replace-parent [this replacement]
    "Replace any apparition of the parent selecor it contains"))