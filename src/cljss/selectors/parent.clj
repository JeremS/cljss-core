(ns cljss.selectors.parent
  (:use cljss.protocols
        cljss.selectors.combination
        cljss.selectors.types))

(defrecord ParentSelector []
  Neutral
  (neutral? [this] false)
  
  SimplifyAble
  (simplify [this] this)
  
  Parent
  (parent? [this] true)
  (replace-parent [this replacement] replacement)
  
  CssSelector
  (compile-as-selector [this]
    (throw (Exception. (str "Parent selector can't be compiled. "
                            "Theres a bug, it should have been eliminated "
                            "during precompilation. Report issue.")))))

(def & (ParentSelector.))


(derive ParentSelector simple-t)