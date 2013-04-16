(ns cljss.selectors.parent
  (:use cljss.selectors.combination
        cljss.selectors.types
        cljss.compilation.protocols))

(defrecord ParentSelector [])

(def & (ParentSelector.))


(derive ParentSelector simple-t)

