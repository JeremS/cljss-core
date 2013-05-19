;; ## Selector types
;; We define here different types of selectors.
;; It is used in the combination part.

(ns cljss.selectors.types
  (:use cljss.selectors.protocols))

(def neutral-t     ::neutral)
(def sel-t         ::sel)
(def simple-t      ::simple-sel)
(def set-t         ::set)

(derive simple-t      sel-t)
(derive set-t         sel-t)


(defn selector-type [sel]
  (if (neutral? sel)
    neutral-t
    (type sel)))