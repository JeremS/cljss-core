(ns cljss.selectors.parent
  (:use cljss.selectors.combination
        cljss.compilation.protocols))

(defrecord ParentSelector [])

(def & (ParentSelector.))


(derive ParentSelector simple-sel-type)

