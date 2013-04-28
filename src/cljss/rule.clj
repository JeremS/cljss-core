(ns cljss.rule
  (:use cljss.protocols
        [cljss.precompilation :only (decorate-rule)]
        [cljss.compilation :only (depth)]))


(declare compile-property-map)


(defrecord Rule [selector properties sub-rules]
  DecorAble
  (decorate [this d]
    (decorate-rule this d))
  
  CSS
  (css-compile [this {start :start-properties 
                   i :indent 
                   :as style}]
    (let [d (depth this)
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


(defmulti consume-properties 
  "When parsing a rule, consume property 
  declarations and sub rule declarations
  to add them to the rule resulted of the parsing."
  (fn [s rule] (type (first s))))


(defn parse-rule 
  "Parse a rule expressed with a vector and returns a Rule."
  [[selection & props-sub-rules]]
  (consume-properties props-sub-rules (rule selection)))

(defmethod consume-properties :default [s r] r)

(defmethod consume-properties clojure.lang.Keyword [[fst scd & rst] r]
  (let [r (assoc-in r [:properties fst] scd)]
    (consume-properties rst r)))


(defmethod consume-properties clojure.lang.PersistentList [[fst scd & rst] r]
  (let [props (apply assoc(:properties r) fst)
        r (assoc r :properties props)]
    (consume-properties (cons scd rst) r)))

(defmethod consume-properties clojure.lang.IPersistentMap [[fst scd & rst] r]
  (let [props (merge (:properties r) fst)
        r (assoc r :properties props)]
    (consume-properties (cons scd rst) r)))


(defmethod consume-properties clojure.lang.PersistentVector [[fst scd & rst] r]
  (let [r (update-in r [:sub-rules] conj (parse-rule fst))]
    (consume-properties (cons scd rst) r)))




