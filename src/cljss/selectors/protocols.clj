(ns cljss.selectors.protocols)

(defprotocol Neutral
  (neutral? [this] 
    "True if the selector is a neutral element in a composition of selectors"))