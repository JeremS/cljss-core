;; ## Precompilation
;; This namespace regroups the different transformation that
;; are applied to an AST in order to ready it for compiation.

(ns ^{:author "Jeremy Schoffen."}
  cljss.precompilation
  (:require [cljss.selectors :refer (& combine)]
            [cljss.AST :refer (rule)])
  (:use cljss.protocols)
  (:import [cljss.AST Rule Query]))

;; ### Visitor implementation
;; In order to work with the AST, an implmentation
;; of the visitor pattern is used.
;; This implementation uses multi method under the covers.


;; Helper used to create unique enviromnment for combined visitors.

(defn- uuid [] (java.util.UUID/randomUUID))


;; A visitor is composed of a visit function and a
;; default environment for the visit of a root node.

(defrecord Visitor [env f])


;; Constructor of a visitor. Here we wrap the visit function
;; so that is sees its own environment. This way, composed visitor
;; work with their own isolates env.

(defn make-visitor
  ([f] (make-visitor {} f))
  ([env f]
   (let [id (uuid)
         env {id env}]
     (Visitor. env
      (fn [v general-env]
        (let [local (get general-env id)
              [new-v new-local]  (f v local)
              new-general (assoc general-env id new-local)]
          (list new-v new-general)))))))

;; Visit of a node given a visitor.
;; We can see here that the visit of a node produces
;; a new version of the node and a new environment.
;; This new environment is used for the visits
;; of the sub elements instead of the giben env.
;; It is handy we a visitor needs to pass on information
;; for the visit of sub elements.

(defn visit [node {f :f env :env :as visitor}]
  (if-not (satisfies? Tree node)
    node
    (let [[new-node new-env] (f node env)
          visitor (assoc visitor :env new-env)
          new-children (mapv #(visit % visitor)
                            (children new-node))]
      (assoc-children new-node new-children))))

;; Visitor composition
;; The goal here mimic function composition
;; for visitors. The difference here is that
;; the composition is done left to right.
;; We can see here that the result of one visit (visitor v1)
;; is passed to the visitor v2.

(defn- chain-2-visitors [v1 v2]
  (let [{f1 :f env1 :env} v1
        {f2 :f env2 :env} v2]
    (Visitor. (merge env1 env2)
     (fn [r env]
       (let [[r env] (f1 r env)]
         (f2 r env))))))

(defn chain-visitors
  "Allows to compose from left to right
  the behaviour of visitors."
  [v1 v2 & vs]
  (reduce chain-2-visitors
          (list* v1 v2 vs)))


(defn- type-first [& args]
  (type (first args)))


(defn- multi-name [v-name]
  (symbol (str "mm-" v-name)))


(defmacro defvisitor
  "Declaration of a visitor.
  A multimethod is created for the visits and
  the said visitor too.

  Note that the created multimethod dispatches
  on the type of the first parameter wich is an AST node."
  [v-name env]
  (let [mm-name (multi-name v-name)]
  `(do
     (defmulti ~mm-name type-first)
     (def ~v-name (make-visitor ~env ~mm-name)))))

(defmacro defvisit
  "Define the behaviour of a visitior in function of
  the first arg type."
  [v-name v-type args & body]
  (let [mm-name (multi-name v-name)]
    `(defmethod ~mm-name ~v-type ~args ~@body)))


;; ### Visitors definition.

;; Adds to rules and their sub rules
;; their level of nesting.
;; The depth is used when compiling the AST
;; to compute indentation. Here the visitor's environment
;; is the current depth.

(defvisitor assoc-depth 0)

(defvisit assoc-depth :default [node depth]
  (list (assoc node :depth depth)
        (inc depth)))


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

;; ### Flattening
;; When an AST is visited, we flatten it
;; to extract rubrules from rule. It allows
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
