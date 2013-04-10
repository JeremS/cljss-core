(ns cljss.precompilation
  (:require [cljss.selectors :as sel]
            [clojure.string :as string])
  (:use cljss.data
        [cljss.precompilation.decorator :only 
         (decorator chain-decorators decorate-rule)]))


(def depth-decorator
  "Attach to a rule its depth, level in which
  it is embeded."
  (decorator {:depth 0}
   (fn [r {d :depth :as env}]
     (list (assoc r :depth d)
           (update-in env [:depth] inc)))))

(def combine-selector-decorator
  "This decorator is used to combine the selectors of sub rules
  with those of their ancestors"
  (decorator {:parent-sel []}
   (fn [{sel :selector :as r} 
        {parent-sel :parent-sel :as env}]
     (let [new-sel (sel/combine parent-sel sel)]
       (list (assoc r :selector new-sel)
             (assoc-in env [:parent-sel] new-sel))))))


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

(defn precompile-rule 
  "Decorate a rule then flattens it."
  [r]
  (-> r
      (decorate-rule default-decorator)
      (flatten-rule)))


