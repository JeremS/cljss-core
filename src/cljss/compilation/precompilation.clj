(ns cljss.compilation.precompilation
  (:require [cljss.selectors :as sel]
            [clojure.string :as string])
  (:use cljss.data))

(defrecord Decorator [f env])

(defn decorator
  "Construct a decorator, is a function that 
  decorate, transforms a rule given a environment.
  the environment env, must be a map.
  
  When no environment is provided
  an empty map is used as the default one."
  ([f]
   (Decorator. f {}))
  ([f env]
   (Decorator. f env)))


(defn- chain-2-decorators [d1 d2]
  (let [{f1 :f env1 :env} d1
        {f2 :f env2 :env} d2]
    (decorator
     (fn [r env]
       (let [[r env] (f1 r env)]
         (f2 r env)))
     (merge env1 env2))))

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
  (decorator 
   (fn [r {d :depth :as env}]
     (list (assoc r :depth d)
           (update-in env [:depth] inc)))
   {:depth 0}))

(def combine-selector-decorator
  "This decorator is used to combine the selector of sub rules
  with those of its ancestors"
  (decorator
   (fn [{sel :selector :as r} 
        {ancs :ancestors :as env}]
     (let [new-ancs (conj ancs sel)
           new-sel (reduce sel/combine new-ancs)]
       (list (assoc r :selector new-sel)
             (assoc-in env [:ancestors] new-ancs))))
   {:ancestors []}))


(def default-decorator
  (chain-decorators combine-selector-decorator depth-decorator))


(defn flatten-rule 
  "Given a rule returns a flatten list of the rule and its
  sub rules"
  [{:as r}]
  (let [new-r (assoc r :sub-rules '())
        sub-rs (:sub-rules r)]
    (cons new-r
          (mapcat flatten-rule sub-rs))))

(defn flatten-rules 
  "See flatten-rule"
  [rs]
  (mapcat #(flatten-rule % 0) rs))

