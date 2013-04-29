(ns cljss.rule
  (:use cljss.protocols))


(declare compile-property-map)


(defrecord Rule [selector properties sub-rules]
  Node
  (children-key [this] :sub-rules)
  (node? [_] true)
  
  CSS
  (css-compile [this {start :start-properties 
                      i :indent 
                      :as style}]
    (let [d (:depth this)
          general-indent      (apply str (repeat d i))
          compiled-selector   (compile-as-selector selector)
          compiled-properties (compile-property-map properties 
                                                    (assoc style 
                                                      :general-indent general-indent))]
        
    (str general-indent compiled-selector " {" start
         compiled-properties 
         general-indent "}"))))



(defn rule 
  ([selection ]
   (rule selection {}))
  ([selection properties]
   (rule selection properties []))
  ([selection properties sub-rules]
   (Rule. selection properties sub-rules)))


(defrecord Query [selector body properties sub-rules]
  Node
  (children-key [this] :body)
  
  CSS
  (css-compile [this {sep :rules-separator :as style}]
    (let [d (or (:depth this) 0)]
      (str selector " {"
             (map #(css-compile % (assoc style :depth d)) body)
           \} sep))))

(defn media [sel & body]
  (Query. sel (vec body) {} []))



(defn compile-property [[p-name p-val]]
  (let [s-name (compile-as-property-name p-name)
        s-val  (compile-as-property-value p-val)]
    (str s-name ": " s-val \;)))


(defn compile-property-map [m style]
  (let [{i  :indent
         gi :general-indent 
         sep :property-separator} style]
    (->> m
         (map compile-property )
         (mapcat #(list gi i % sep ))
         (apply str))))



