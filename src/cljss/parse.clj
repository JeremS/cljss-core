;; Parsing
;; This namespace regroups the necessary functions
;; used to parse the dsl in order to produce an AST.

(ns ^{:author "Jeremy Schoffen."}
  cljss.parse
  (:use [cljss.AST :only (rule inline-css)])
  (:import [cljss.AST Query InlineCss CssComment]))

;; ### Parsing functions

(defmulti consume-properties
  "When parsing a rule, consume property
  declarations and sub rule declarations
  to add them to the rule resulted of the parsing."
  (fn [s rule] (type (first s))))

(defmulti parse-rule
  "Parse a rule expressed with a vector or a media query and returns
  a tree represntation."
  type)


;; Generic types used for dispatch

(derive clojure.lang.LazySeq ::list)
(derive clojure.lang.PersistentList ::list)

(derive String ::inline)
(derive Character ::inline)
(derive InlineCss ::inline)
(derive CssComment ::inline)


;; ### Parsing of rules
;; A vector is considered a rule.

(defmethod parse-rule clojure.lang.PersistentVector [[selection & props-sub-rules]]
  (consume-properties props-sub-rules (rule selection)))


;; Parsing of a media query.

(defmethod parse-rule cljss.AST.Query [{body :body :as query}]
  (consume-properties body (assoc query :body nil)))


;; Parsing of a list of rules

(defmethod parse-rule ::list [rules]
  (map parse-rule rules))



;; A string is considered inline css

(defmethod parse-rule ::inline [i] i)
(defmethod parse-rule String [s] (inline-css s))
(defmethod parse-rule Character [c] (inline-css (str c)))



;; ### Parsing of the inside of rules

;; If we don't recognize what is parsed an exception is thrown.

(defmethod consume-properties :default [stream rule]
  (throw
   (ex-info
    (str (type (first stream))
         " can't be a css property name/value.")
    {:properties stream
     :rule rule})))

;; End of a rule we return the rule.

(defmethod consume-properties nil [stream rule] rule)


;; Parsing of a property name

(defmethod consume-properties clojure.lang.Keyword [[fst scd & rst] node]
  (let [node (assoc-in node [:properties fst] scd)]
    (consume-properties rst node)))


;; We eliminate lists as if they weren't there.

(defmethod consume-properties ::list [[a-list & rst] node]
    (consume-properties (concat a-list rst) node))


;; A map is merged directly in the properties of the constructed rule.

(defmethod consume-properties clojure.lang.IPersistentMap [[fst scd & rst] node]
  (let [props (merge (:properties node) fst)
        node (assoc node :properties props)]
    (consume-properties (cons scd rst) node)))


;; A vector is considered a sub rule.

(defmethod consume-properties clojure.lang.PersistentVector [[fst & rst] node]
  (let [node (update-in node [:sub-rules] conj (parse-rule fst))]
    (consume-properties rst node)))


;; A media query is a sub rule.

(defmethod consume-properties cljss.AST.Query [[fst & rst] node]
  (let [node (update-in node [:sub-rules] conj (parse-rule fst))]
    (consume-properties rst node)))


;; Inline stuff

(defmethod consume-properties ::inline [[fst & rst] node]
  (let [node (update-in node [:sub-rules] conj (parse-rule fst))]
    (consume-properties rst node)))


(defn parse-rules [rules]
  (->> rules
       (map parse-rule)
       flatten))
