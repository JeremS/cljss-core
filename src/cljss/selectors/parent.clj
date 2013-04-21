(ns cljss.selectors.parent
  (:use cljss.selectors.combination
        cljss.selectors.types
        cljss.selectors.protocols
        cljss.compilation.protocols
        [cljss.precompilation.decorator :only (decorator)]))

(defrecord ParentSelector []
  Neutral
  (neutral? [this] false)
  
  SimplifyAble
  (simplify [this] this)
  
  Parent
  (parent? [this] true)
  
  CssSelector
  (compile-as-selector [this]
    (throw (Exception. (str "Parent selector can't be compiled. "
                            "Theres a bug, it should have been eliminated "
                            "during precompilation. Report issue.")))))

(def & (ParentSelector.))


(derive ParentSelector simple-t)

(defn replace-parent-selector [rule parent-sel]
  (throw (Exception. "Not implemented yet.")))