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
     (list (assoc r ::depth depth) 
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

(defn compile-rule [{:keys [selector properties] :as r}]
  (let [depth (::depth r)]
    (binding [*general-indent* (apply str (repeat depth *indent*))]
      (let [compiled-selector   (compile-as-selector selector)
            compiled-properties (compile-property-map properties)]
        (str *general-indent* compiled-selector " {" *start-properties*
                                  compiled-properties 
             *general-indent* "}")))))

