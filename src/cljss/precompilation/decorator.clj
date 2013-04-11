(ns cljss.precompilation.decorator
  (:require [cljss.selectors :as sel])
  (:use clojure.tools.trace))

(defrecord Decorator [env f])

(defn- uuid [] (java.util.UUID/randomUUID))


(defn decorator
  "Construct a decorator, is a function that 
  decorate, transforms a rule given a environment.
  the environment env, must be a map.
  
  When no environment is provided
  an empty map is used as the default one."
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
  the behaviour of decorators.
  
  Be careful, the default environments of each
  decorators are merged, if they "
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

(def depth-decorator
  "Attach to a rule its depth, level in which
  it is embeded."
  (decorator 0
   (fn [r depth]
     (list (assoc r :depth depth) (inc depth)))))

(def combine-selector-decorator
  "This decorator is used to combine the selectors of sub rules
  with those of their ancestors."
  (decorator []
   (fn [{sel :selector :as r} parent-sel]
     (let [new-sel (sel/combine parent-sel sel)]
       (list (assoc r :selector new-sel)
             new-sel)))))


(def assoc-parent-selector-decorator
  (decorator []
    (fn [r parent-sel]
      (list (assoc r :parent-sel parent-sel)
            (:selector r)))))

(def default-decorator
  (chain-decorators combine-selector-decorator 
                    depth-decorator
                    assoc-parent-selector-decorator))



