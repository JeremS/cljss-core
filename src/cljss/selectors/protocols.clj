(ns cljss.selectors.protocols)

(defprotocol Parent
  (parent? [this]
    "Detects if the parent selector is used.")
  (replace-parent [this replacement]
    "Replace any apparition of the parent selecor it contains"))