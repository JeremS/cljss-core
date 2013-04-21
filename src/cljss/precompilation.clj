(ns cljss.precompilation
  (:require [clojure.string :as string]
            [cljss.precompilation.decorator :as d]
            [cljss.selectors.combination :as sel])
  (:use [cljss.selectors :only (combine-selector-decorator)]
        [cljss.compilation :only (depth-decorator)]))


(def assoc-parent-selector-decorator
  (d/decorator []
    (fn [r parent-sel]
      (list (assoc r :parent-sel parent-sel)
            (:selector r)))))


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


