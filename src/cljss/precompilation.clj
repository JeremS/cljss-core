(ns cljss.precompilation
  (:require [clojure.string :as string]
            [cljss.precompilation.decorator :as d]
            [cljss.selectors.combination :as sel])
  (:use [cljss.selectors :only (combine-selector-decorator)]))


(def assoc-parent-selector-decorator
  (d/decorator []
    (fn [r parent-sel]
      (list (assoc r :parent-sel parent-sel)
            (:selector r)))))

(def depth-decorator
  "Attach to a rule its depth, level in which
  it is embeded. 
  
  This decorator is used when a rule is compiled, 
  the depth being used to compute indentation."
  (d/decorator 0
   (fn [r depth]
     (list (assoc r :depth depth) 
           (inc depth)))))
        


(def default-decorator
  (d/chain-decorators combine-selector-decorator 
                      depth-decorator
                      assoc-parent-selector-decorator))


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
  [r]
  (-> r
      (d/decorate-rule default-decorator)
      (flatten-rule)))


