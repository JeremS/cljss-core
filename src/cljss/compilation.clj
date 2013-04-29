(ns cljss.compilation
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.compilation.utils))


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




