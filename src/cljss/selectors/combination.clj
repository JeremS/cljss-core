;; ## Combination
;; Implementation of a generic combination of selectors.
;; Combination is used to combine rules selectors with
;; the ones of their sub rules.

(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.combination
  (:use cljss.selectors.types))



(defmulti combine
  "Combine two selector in a way that sel1 is the parent selector
  and sel2 is the child selector. This function is used determine
  selectors when we deal with nested rules.

  For instance, when we flatten the css rule:
  [:div :color :blue
   [:a :color :red]]

  we get the two rules:
  [:div :color :blue]
  [(combine :div :a) :color :red]"
  (fn [sel1 sel2] [(selector-type sel1)(selector-type sel2)]))


(defmethod combine [sel-t neutral-t] [k _] k)
(defmethod combine [neutral-t sel-t] [_ k] k)


(defmethod combine [sel-t sel-t] [v1 v2] [v1 v2])


(defmethod combine [sel-t  set-t] [k s] (set (for [sel s] (combine k sel))))
(defmethod combine [set-t  sel-t] [s k] (set (for [sel s] (combine sel k))))

(defmethod combine [set-t set-t] [s1 s2] (set (for [e1 s1 e2 s2] (combine e1 e2))))


