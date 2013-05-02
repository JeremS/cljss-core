(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.types
  (:use cljss.protocols))

(def neutral-t     ::neutral)
(def sel-t         ::sel)
(def simple-t      ::simple-sel)
(def combination-t ::combination)
(def set-t         ::set)

(derive simple-t      sel-t)
(derive combination-t sel-t)
(derive set-t         sel-t)


(defn selector-type [sel]
  (if (or (neutral? sel) (nil? sel))
    neutral-t
    (type sel)))