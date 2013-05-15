;; ## Precompilation
;; This namespace regroups the different transformations that
;; are applied to an AST in order to ready it for compilation.

(ns ^{:author "Jeremy Schoffen."}
  cljss.precompilation
  (:require [cljss.selectors :refer (& combine)]
            [cljss.AST :refer (rule)])
  (:use [cljss.precompilation protocols visitor]
        cljss.selectors.protocols)
  (:import [cljss.AST Rule Query]))



;; Adds to rules and their sub rules
;; their level of nesting.
;; The depth is used when compiling the AST
;; to compute indentation. Here the visitor's environment
;; is the current depth.

(defvisitor assoc-depth 0)


(defvisit assoc-depth :default [node depth]
  (list (assoc node :depth depth)
        (inc depth)))

(defvisit assoc-depth Rule [node depth]
  (list (assoc node :depth depth)
        (if (-> node :properties seq)
          (inc depth)
          depth)))


;; Combine selectors of a rule to its subrules
;; recursively.

(defvisitor combine-or-replace-parent [])

(defvisit combine-or-replace-parent :default
  [node parent-sel]
  (list node parent-sel))


(defn- combine-or-replace [sel parent-sel]
  (if (parent? sel)
    (replace-parent sel parent-sel)
    (combine parent-sel sel)))

(defvisit combine-or-replace-parent Rule
  [{sel :selector :as rule}
   parent-sel]
  (let [new-sel (combine-or-replace sel parent-sel)]
    (list (assoc rule :selector new-sel)
          new-sel)))


;; Symplify (or compute) the definite selector
;; of a rule and its sub rules recursilely.
;; We realize here the expansion of sets selectors.

(defvisitor simplify-selector nil)

(defvisit simplify-selector :default
  [node env]
  (list node env))

(defvisit simplify-selector Rule
  [{sel :selector :as rule} env]
  (list (assoc rule
          :selector (simplify sel))
        env))


;; In the case of a media query with properties,
;; we remove the properties of the media query
;; and create a sub rule with those properties
;; and the parent selecor as selector.

(defvisitor make-rule-for-media-properties nil)

(defvisit make-rule-for-media-properties Rule
  [{sel :selector :as rule} parent-selector]
  (list rule sel))

(defvisit make-rule-for-media-properties Query
 [{props :properties sr :sub-rules :as query} parent-sel]
  (if (seq props)
    (let [new-rule (rule & props)
          new-sub-rules (conj sr new-rule)]
      (list (assoc query
              :sub-rules new-sub-rules
              :properties nil)
            parent-sel))
    (list query parent-sel)))


(def default-visitor
  "The visitor used to precompile an AST, representation
  of a rule and its sub rules."
  (chain-visitors
     combine-or-replace-parent
     simplify-selector
     make-rule-for-media-properties
     assoc-depth))


;; ### AST flattening
;; After an AST has been visited, we flatten it
;; to extract rubrules from their parent rule. It allows
;; for a simpler algorithm to compile rules.

(defmulti flatten-AST
  "Flattens a rule to ready for compilation."
  type)

(defmethod flatten-AST :default [node]
  (list node))

(defmethod flatten-AST Rule
  [{sr :sub-rules :as rule}]
  (cons (assoc rule :sub-rules '())
        (mapcat flatten-AST sr)))

(defmethod flatten-AST Query [{sr :sub-rules :as query}]
  (list (assoc query
          :sub-rules (mapcat flatten-AST sr))))

(defn precompile-rule
  "Applies the defaut visitor to a rule then flattens it."
  [rule]
  (-> rule
      (visit default-visitor)
      (flatten-AST)))

(defn precompile-rules
  "Takes a seq of rules in their AST form, precompile them
  then flattens them."
  [rules]
  (mapcat precompile-rule rules))
