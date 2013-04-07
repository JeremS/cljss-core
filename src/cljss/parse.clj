(ns cljss.parse
  (:use cljss.data
        clojure.tools.trace))


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


(defn parse [rules]
  (map parse-rule rules))
