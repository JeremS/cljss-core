(ns cljss.selectors)



(defrecord GluedSelectors [fst nxt])




(def neutral-type ::neutral)
(def sel-type ::sel)
(def simple-sel-type ::simple-sel)
(def path-type ::path)
(def set-type ::set)


(defmulti selector-type
  "Given a selector, more precisely (type selector) returns
  a keyword representing the kind of selector that this selector is.
  This function is used by the combine function."
  type)

(defmethod selector-type :default [sel] 
  (throw (Exception. (str "No type defined for: " (type sel)))))

(defmethod selector-type String                        [_] ::simple-sel)
(defmethod selector-type clojure.lang.Keyword          [_] ::simple-sel)
(defmethod selector-type clojure.lang.PersistentVector [x] (if (seq x) ::path ::neutral))
(defmethod selector-type clojure.lang.IPersistentSet   [x] (if (seq x) ::set  ::neutral))


(defmethod selector-type GluedSelectors                [_] ::simple-sel)



(derive ::simple-sel ::sel)
(derive ::path       ::sel)
(derive ::set        ::sel)

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


(defmethod combine [::sel ::neutral] [k _] k)
(defmethod combine [::neutral ::sel] [_ k] k)


(defmethod combine [::simple-sel ::simple-sel] [k1 k2] [k1 k2])

(defmethod combine [::simple-sel ::path      ] [k v] (combine [k] v))
(defmethod combine [::simple-sel ::set       ] [k s] (combine [k] s))
(defmethod combine [::path       ::simple-sel] [v k] (combine v [k]))
(defmethod combine [::set        ::simple-sel] [s k] (combine s [k]))


(defmethod combine [::path ::path]
  [v1 v2]
  (vec (concat v1 v2)))

(defmethod combine [::set ::set] [s1 s2]
  (set (for [e1 s1 e2 s2]
         (combine e1 e2))))

(defmethod combine [::set ::path] [s v]
  (set (reduce #(conj %1 (combine %2 v)) 
               #{} 
               s)))

(defmethod combine [::path ::set] [v s]
  (set (reduce #(conj %1 (combine v %2)) 
               #{} 
               s)))
