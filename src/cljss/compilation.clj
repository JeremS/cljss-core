(ns cljss.compilation
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.compilation.utils
        [cljss.precompilation :only (decorator)]
        [cljss.compilation.styles :only (compressed-style)]))


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




(defn compile-property [[p-name p-val]]
  (let [s-name (compile-as-property-name p-name)
        s-val  (compile-as-property-value p-val)]
    (str s-name ": " s-val \;)))


(defn compile-property-map 
  ([m]
   (compile-property-map m compressed-style))
  ([m style]
   (let [{i  :indent 
          gi :general-indent 
          sep :property-separator} style]
     (->> m
          (map compile-property )
          (mapcat #(list gi i % sep ))
          (apply str)))))

(defn compile-rule 
  ([rule]
   (compile-rule rule compressed-style))
  ([{:keys [selector properties] :as r} 
    {start :start-properties i :indent
     :as style}]
   (let [depth (::depth r)
         general-indent (apply str (repeat depth i))
         compiled-selector
            (compile-as-selector selector)
         compiled-properties 
            (compile-property-map properties 
                                  (assoc style :general-indent general-indent))]
        
     (str general-indent compiled-selector " {" start
          compiled-properties 
          general-indent "}"))))

(defn compile-rules 
  ([rules]
   (compile-rules rules compressed-style))
  ([rules {sep :rules-separator :as style}]
   (->> rules
        (map #(compile-rule % style))
        (string/join sep ))))
