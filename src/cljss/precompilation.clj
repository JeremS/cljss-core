(ns cljss.precompilation
  (:require cljss.selectors.basic
            cljss.selectors.combinators
            [cljss.selectors.parent :refer [&]]
            [cljss.AST :as ast])
  (:use cljss.protocols
        clojure.tools.trace
        [clojure.pprint :only (pprint)]
        [cljss.selectors.combination :only (combine)])
  (:import [cljss.AST Rule Query]))

(defn- uuid [] (java.util.UUID/randomUUID))

(defrecord Visitor [env f])

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


(defn visit [node {f :f env :env :as visitor}]
  (if-not (satisfies? Tree node)
    node
    (let [[new-node new-env] (f node env)
          visitor (assoc visitor :env new-env)
          new-children (mapv #(visit % visitor) 
                            (children new-node))]
      (assoc-children new-node new-children))))

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


(defmacro defvisitor [v-name env]
  (let [mm-name (multi-name v-name)]
  `(do
     (defmulti ~mm-name type-first)
     (def ~v-name (make-visitor ~env ~mm-name)))))

(defmacro defvisit [v-name v-type args & body]
  (let [mm-name (multi-name v-name)]
    `(defmethod ~mm-name ~v-type ~args ~@body)))


(defvisitor assoc-depth 0)

(defvisit assoc-depth :default [node depth]
  (list (assoc node :depth depth)
        (inc depth)))


(defn- combine-or-replace [sel parent-sel]
  (if-not (parent? sel)
    (combine parent-sel sel)
    (replace-parent sel parent-sel)))

(defvisitor combine-or-replace-parent [])

(defvisit combine-or-replace-parent cljss.AST.Rule 
  [{sel :selector :as rule} 
   parent-sel]
  (let [new-sel (combine-or-replace sel parent-sel)]
    (list (assoc rule :selector new-sel)
          new-sel)))

(defvisit combine-or-replace-parent :default
  [node parent-sel]
  (list node parent-sel))



(defvisitor simplify-selector nil)

(defvisit simplify-selector :default
  [node env]
  (list node env))

(defvisit simplify-selector cljss.AST.Rule
  [{sel :selector :as rule} env]
  (list (assoc rule 
          :selector (simplify sel))
        env))


(defvisitor make-rule-for-media-properties nil)

(defvisit make-rule-for-media-properties cljss.AST.Rule
  [{sel :selector :as rule} parent-selector]
  (list rule sel))

(defvisit make-rule-for-media-properties cljss.AST.Query
 [{props :properties sr :sub-rules :as query} parent-sel]
  (let [new-rule (ast/rule & props)
        new-sub-rules (conj sr new-rule)]
    (list (assoc query 
            :sub-rules new-sub-rules
            :properties nil)
          parent-sel)))


(def default-visitor
  (chain-visitors
     combine-or-replace-parent
     simplify-selector
     make-rule-for-media-properties
     assoc-depth))

(defmulti flatten-AST type)

(defmethod flatten-AST Rule 
  [{sr :sub-rules :as rule}]
  (cons (assoc rule :sub-rules '())
        (mapcat flatten-AST sr)))

(defmethod flatten-AST Query [{sr :sub-rules :as query}]
  (list (assoc query 
          :sub-rules (mapcat flatten-AST sr))))

(defn precompile-rule 
  "Decorate a rule then flattens it."
  [rule]
  (-> rule
      (visit default-visitor)
      (flatten-AST)))
