(ns cljss.selectors.combination
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
  [[:div :a] :color :red]
  
  The selector [:div :a] of the second rule is the result of 
  (combine :div :a), the combination of the parent selector :div 
  and the child selector :a"
  (fn [sel1 sel2] [(selector-type sel1)(selector-type sel2)]))


(defmethod combine [sel-t neutral-t] [k _] k)
(defmethod combine [neutral-t sel-t] [_ k] k)


(defmethod combine [simple-t simple-t] [k1 k2] [k1 k2])

(defmethod combine [simple-t  combination-t] [k v] (combine [k] v))
(defmethod combine [simple-t  set-t        ] [k s] (combine [k] s))
(defmethod combine [combination-t simple-t ] [v k] (combine v [k]))
(defmethod combine [set-t         simple-t ] [s k] (combine s [k]))

(defmethod combine [combination-t combination-t]
  [v1 v2]
  [v1 v2])

(defmethod combine [set-t set-t] [s1 s2]
  (set (for [e1 s1 e2 s2]
         (combine e1 e2))))

(defmethod combine [set-t combination-t] [s v]
  (set (reduce #(conj %1 (combine %2 v)) 
               #{} 
               s)))

(defmethod combine [combination-t set-t] [v s]
  (set (reduce #(conj %1 (combine v %2)) 
               #{} 
               s)))

