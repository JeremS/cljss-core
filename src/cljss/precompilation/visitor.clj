;; ## Visitor implementation
;; In order to work with the AST, an implementation
;; of the visitor pattern is used.
;; This implementation uses multi method under the covers.


(ns cljss.precompilation.visitor
  (:use cljss.precompilation.protocols))

;; Helper used to create unique enviromnment for combined visitors.

(defn- uuid [] (java.util.UUID/randomUUID))


;; A visitor is composed of function applied to a node when visited
;; and a default environment for the visit of a root node.

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
;; of the sub elements instead of the given env.
;; It is handy when a visitor needs to pass on information
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
;; The goal here is to mimic function composition
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



(defn- multi-name [v-name]
  (symbol (str "mm-" v-name)))


(defmacro defvisitor
  "Declaration of a visitor.
  A multimethod is created for the visits and
  the said visitor too.

  Note that the created multimethod dispatches
  on the type of the first parameter wich should be
  an AST node."
  [v-name env]
  (let [mm-name (multi-name v-name)]
  `(do
     (defmulti ~mm-name (fn [& ~'args] (type (first ~'args))))
     (def ~v-name (make-visitor ~env ~mm-name)))))

(defmacro defvisit
  "Defines the behaviour of a visitior in function of
  the first arg type."
  [v-name v-type args & body]
  (let [mm-name (multi-name v-name)]
    `(defmethod ~mm-name ~v-type ~args ~@body)))