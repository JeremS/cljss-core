(ns cljss.selectors.parent
  (:use cljss.selectors.combination
        cljss.compilation.protocols))

(defrecord ParentSelector [])

(def & (ParentSelector.))


(defmethod mm-selector-type ParentSelector [_] simple-sel-type)

