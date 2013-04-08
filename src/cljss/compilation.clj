(ns cljss.compilation
  (:require [cljss.selectors :as sel]
            [clojure.string :as string])
  (:use cljss.compilation.protocols))


(defn compile-seq-then-join
  "Compile each value of a collection using compile-fn,
  then join the results with the string s."
  [vs compile-fn s]
  (->> vs
       (map compile-fn)
       (string/join s)))

(defn compile-path-sel
  "Compile a path like selector."
  [sel]
  (compile-seq-then-join sel 
                         compile-as-selector
                         \space))

(defn compile-set-sel
  "Compile a set selector."
  [sel]
  (compile-seq-then-join sel 
                         compile-as-selector
                         ", "))

(defn compile-seq-property-value
  "Compile a collection representing a property's value."
  [s]
  (compile-seq-then-join s
                         compile-as-property-value 
                         \space))


(extend-protocol CssSelector
  String
  (compile-as-selector [this] this)
  
  clojure.lang.Keyword
  (compile-as-selector [this] (name this))
  
  clojure.lang.PersistentVector
  (compile-as-selector [this]
    (compile-path-sel this))
  
  clojure.lang.IPersistentSet
  (compile-as-selector [this]
    (compile-set-sel this)))


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



(def ^:dynamic *end-property-line* "")
(def ^:dynamic *start-properties* "")
(def ^:dynamic *end-properties* "")
(def ^:dynamic *general-indent* "")
(def ^:dynamic *indent* "")
(def ^:dynamic *property-indent* "")

(defn compile-property [[p-name p-val]]
  (let [s-name (compile-as-property-name p-name)
        s-val  (compile-as-property-value p-val)]
    (str s-name ": " s-val \; *end-property-line*)))



(defn- add-property-indent [props]
  (interleave (repeat (str *general-indent* *property-indent*))
               props))

(defn compile-property-map [m]
  (->> m
       (map compile-property )
       (add-property-indent )
       (string/join )))

(defn compile-rule [{:keys [selector properties depth]}]
  (binding [*general-indent* (apply str (repeat depth *indent*))]
    (let [compiled-selector   (compile-as-selector selector)
          compiled-properties (compile-property-map properties)]
      (str *general-indent* compiled-selector " {" *start-properties*
                                compiled-properties 
           *general-indent* "}"))))

