(ns cljss.selectors.combination
  (:use cljss.selectors.simple-selectors)
  (:import [cljss.selectors.simple_selectors 
            Children Siblings GSiblings]))

(defprotocol Neutral
  (neutral? [this]))


(extend-protocol Neutral
  String
  (neutral? [this] (-> this seq not))
  
  clojure.lang.Keyword
  (neutral? [_] false)
  
  clojure.lang.PersistentVector
  (neutral? [this] (-> this seq not))
 
  clojure.lang.IPersistentSet
  (neutral? [this] (-> this seq not))
  
  cljss.selectors.simple_selectors.Children
  (neutral? [this] (-> this :sels seq not))
  
  cljss.selectors.simple_selectors.Siblings
  (neutral? [this] (-> this :sels seq not))
  
  cljss.selectors.simple_selectors.GSiblings
  (neutral? [this] (-> this :sels seq not)))



(def neutral-type ::neutral)
(def sel-type ::sel)
(def simple-sel-type ::simple-sel)
(def combination-type ::combination)
(def descendant-type ::descandant)
(def set-type ::set)

(derive ::simple-sel  ::sel)
(derive ::combination ::sel)
(derive ::set         ::sel)
(derive ::descendant ::combination)


(defmulti mm-selector-type
  "Given a selector, more precisely (type selector) returns
  a keyword representing the kind of selector that this selector is.
  This function is used by the combine function."
  type)

(defmethod mm-selector-type :default [sel] 
  (throw (Exception. (str "No type defined for: " (type sel)))))

(defmethod mm-selector-type String                        [_] ::simple-sel)
(defmethod mm-selector-type clojure.lang.Keyword          [_] ::simple-sel)
(defmethod mm-selector-type clojure.lang.PersistentVector [x] ::descendant)
(defmethod mm-selector-type clojure.lang.IPersistentSet   [x] ::set)

(defmethod mm-selector-type cljss.selectors.simple_selectors.Children  [x] ::combination)
(defmethod mm-selector-type cljss.selectors.simple_selectors.Siblings  [x] ::combination)
(defmethod mm-selector-type cljss.selectors.simple_selectors.GSiblings [x] ::combination)
(defn selector-type [sel]
  (if (neutral? sel) 
    ::neutral 
    (mm-selector-type sel)))

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

(defmethod combine [::simple-sel  ::combination] [k v] (combine [k] v))
(defmethod combine [::simple-sel  ::set        ] [k s] (combine [k] s))
(defmethod combine [::combination ::simple-sel ] [v k] (combine v [k]))
(defmethod combine [::set         ::simple-sel ] [s k] (combine s [k]))

(defmethod combine [::descendant ::descendant]
  [v1 v2]
  (vec (concat v1 v2)))

(defmethod combine [::combination ::combination]
  [v1 v2]
  [v1 v2])


(defmethod combine [::set ::set] [s1 s2]
  (set (for [e1 s1 e2 s2]
         (combine e1 e2))))

(defmethod combine [::set ::combination] [s v]
  (set (reduce #(conj %1 (combine %2 v)) 
               #{} 
               s)))

(defmethod combine [::combination ::set] [v s]
  (set (reduce #(conj %1 (combine v %2)) 
               #{} 
               s)))

(combine (c-> :div :p) (c-+ :a :span))