(ns ^{:author "Jeremy Schoffen."}
  cljss.parse
  (:use [cljss.AST :only (rule)])
  (:import cljss.AST.Query))


(defmulti consume-properties
  "When parsing a rule, consume property
  declarations and sub rule declarations
  to add them to the rule resulted of the parsing."
  (fn [s rule] (type (first s))))

(defmulti parse-rule
  "Parse a rule expressed with a vector or a media query and returns
  a tree represntation."
  type)


(defmethod parse-rule clojure.lang.PersistentVector [[selection & props-sub-rules]]
  (consume-properties props-sub-rules (rule selection)))

(defmethod parse-rule cljss.AST.Query [{body :body :as query}]
  (consume-properties body (assoc query :body nil)))



(defmethod consume-properties :default [stream rule] rule)

(defmethod consume-properties clojure.lang.Keyword [[fst scd & rst] node]
  (let [node (assoc-in node [:properties fst] scd)]
    (consume-properties rst node)))


(defmethod consume-properties clojure.lang.PersistentList [[fst scd & rst] node]
  (let [props (apply assoc (:properties node) fst)
        node (assoc node :properties props)]
    (consume-properties (cons scd rst) node)))

(defmethod consume-properties clojure.lang.IPersistentMap [[fst scd & rst] node]
  (let [props (merge (:properties node) fst)
        node (assoc node :properties props)]
    (consume-properties (cons scd rst) node)))


(defmethod consume-properties clojure.lang.PersistentVector [[fst scd & rst] node]
  (let [node (update-in node [:sub-rules] conj (parse-rule fst))]
    (consume-properties (cons scd rst) node)))

(defmethod consume-properties cljss.AST.Query [[fst scd & rst] node]
  (let [node (update-in node [:sub-rules] conj (parse-rule fst))]
    (consume-properties (cons scd rst) node)))



(defn parse-rules [rules]
  (map parse-rule rules))
