(ns ^{:author "Jeremy Schoffen."}
  cljss.parse
  (:use [cljss.AST :only (rule inline-css)])
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

;; ### Parsing of rules
;; A vector is considered a rule.

(defmethod parse-rule clojure.lang.PersistentVector [[selection & props-sub-rules]]
  (consume-properties props-sub-rules (rule selection)))


;; Parsing of a media query.

(defmethod parse-rule cljss.AST.Query [{body :body :as query}]
  (consume-properties body (assoc query :body nil)))


;; Parsing of a list of rules

(defmethod parse-rule clojure.lang.PersistentList [rules]
  (map parse-rule rules))

(defmethod parse-rule clojure.lang.LazySeq [rules]
  (map parse-rule rules))


;; A string is considered inline css

(defmethod parse-rule String [s] (inline-css s))




;; ### Parsing of the inside of rules

(defmethod consume-properties :default [stream rule] rule)

(defmethod consume-properties clojure.lang.Keyword [[fst scd & rst] node]
  (let [node (assoc-in node [:properties fst] scd)]
    (consume-properties rst node)))


(defmethod consume-properties clojure.lang.PersistentList [[a-list & rst] node]
    (consume-properties (concat a-list rst) node))

(defmethod consume-properties clojure.lang.LazySeq [[a-list & rst] node]
    (consume-properties (concat a-list rst) node))

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
  (->> rules
       (map parse-rule)
       flatten))


