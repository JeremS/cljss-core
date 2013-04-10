(ns cljss.precompilation
  (:require [cljss.selectors :as sel]
            [clojure.string :as string]
            [cljss.precompilation.decorator :as d]))



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
      (d/decorate-rule d/default-decorator)
      (flatten-rule)))


