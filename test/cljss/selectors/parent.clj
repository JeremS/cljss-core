(ns cljss.selectors.parent
  (:use cljss.selectors
        cljss.compilation.protocols))

(defrecord ParentSelector [])

(def & (ParentSelector.))


(defmethod selector-type ParentSelector [_] simple-sel-type)

