;; ## Parent selector

(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.parent
  (:use cljss.protocols
        cljss.selectors.protocols
        cljss.selectors.combination
        cljss.selectors.types))


(defrecord ParentSelector []
  Neutral
  (neutral? [this] false)

  SimplifyAble
  (simplify [this] this)

  Parent
  (parent? [this] true)
  (replace-parent [this replacement] replacement))

(def & (ParentSelector.))


(derive ParentSelector simple-t)