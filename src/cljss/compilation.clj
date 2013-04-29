(ns cljss.compilation
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.compilation.utils
        [cljss.precompilation :only (decorator)]))

(def depth-decorator
  "Attach to a rule its depth, level in which
  it is embeded. 
  
  This decorator is used when a rule is compiled, 
  the depth being used to compute indentation."
  (decorator 0
   (fn [r depth]
     (list (assoc r :depth depth) 
           (inc depth)))))


(defn compile-seq-property-value
  "Compile a collection representing a property's value."
  [s]
  (compile-seq-then-join s
                         compile-as-property-value 
                         \space))


(extend-protocol CssPropertyName
  clojure.lang.Keyword
  (compile-as-property-name [this] (name this))
  
  String
  (compile-as-property-name [this] this))



(extend-protocol CssPropertyValue
  String
  (compile-as-property-value [this] this)
  
  clojure.lang.Keyword
  (compile-as-property-value [this] (name this))
  
  clojure.lang.PersistentVector
  (compile-as-property-value [this]
    (compile-seq-property-value this))
  
  clojure.lang.PersistentList
  (compile-as-property-value [this]
    (compile-seq-property-value this)))




