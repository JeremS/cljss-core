(ns cljss.selectors
  (:require cljss.selectors.basic
            cljss.selectors.combinators
            [cljss.precompilation.decorator :as d])
  (:use cljss.selectors.protocols
        [cljss.selectors.combination :only (combine)]))


(def combine-selector-decorator
  "This decorator is used to combine the selectors of sub rules
  with those of their ancestors."
  (d/decorator []
   (fn [{sel :selector :as r} parent-sel]
     (let [new-sel (combine parent-sel sel)]
       (list (assoc r :selector new-sel)
             new-sel)))))

(def simplify-selectors-decorator
  (d/decorator
    (fn [r ctxt]
      (list (assoc r 
              :selector (simplify (:selector r)))
            ctxt))))


