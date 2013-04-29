(ns cljss.precompilation
  (:require [clojure.string :as string]
            [cljss.AST :as ast]
            [cljss.parse :as parse])
  (:use cljss.protocols
        clojure.tools.trace)
  (:import cljss.AST.Rule))

(defn- uuid [] (java.util.UUID/randomUUID))

(defn- children [node]
  (get node (children-key node)))

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
  (if-not (node? node)
    node
    (let [[new-node new-env] (f node env)
          visitor (assoc visitor :env new-env)
          new-children (map #(visit % visitor) 
                            (children new-node))]
      (assoc new-node (children-key node) new-children))))

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


(defvisitor depth-visitor 0)

(defvisit depth-visitor :default [node depth]
  (list (assoc node :depth depth)
        (inc depth)))










(defrecord Decorator [env f])




(defn decorator
  "Construct a decorator, from a function that 
  decorate, transforms a rule given a environment.
  
  When no environment is provided
  an empty map is used as the default one.
  
  Decorator are used to process rules trees results of 
  parsing rules as expressed in the dsl."
  ([f] (decorator {} f))
  ([env f]
   (let [id (uuid) 
         env {id env}]                  ; generate a new global env
     (Decorator. env                    ; create decorator with general env and a wrappred decoration function
      (fn [v general-env]               ; the new decoration function takes 
        (let [local (get general-env id); recovers the env for this decorator
              [new-v new-local]  (f v local) ; decorate the value
              new-general (assoc general-env id new-local)] ; create a new value for the general env
          (list new-v new-general))))))) ; returns the new value and the new general env

(defn- chain-2-decorators [d1 d2]
  (let [{f1 :f env1 :env} d1
        {f2 :f env2 :env} d2]
    (Decorator. (merge env1 env2)
     (fn [r env]
       (let [[r env] (f1 r env)]
         (f2 r env))))))

(defn chain-decorators 
  "Allows to compose from left to right
  the behaviour of decorators."
  [d1 d2 & ds]
  (reduce chain-2-decorators 
          (list* d1 d2 ds)))

(defn- dr [r f env]
  (let [[new-r new-env] (f r env)
        new-sub-rules (map #(dr % f new-env)
                          (:sub-rules r))]
    (assoc new-r 
      :sub-rules new-sub-rules)))

(defn decorate-rule 
  "Applies a decorator to a rule and recursively 
  to its sub rules."
  [r {:keys [f env]}]
  (dr r f env))

(defn flatten-rule 
  "Given a rule returns a flatten list of the rule and its
  sub rules"
  [{:as r}]
  (let [new-r (assoc r :sub-rules '())
        sub-rs (:sub-rules r)]
    (cons new-r
          (mapcat flatten-rule sub-rs))))

(defn precompile-rule 
  "Decorate a rule then flattens it."
  [r deco]
  (-> r
      (decorate-rule deco)
      (flatten-rule)))


