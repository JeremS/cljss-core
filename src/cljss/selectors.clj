(ns cljss.selectors
  (:require cljss.selectors.basic
            cljss.selectors.combinators
            [cljss.precompilation.decorator :as d])
  (:use cljss.selectors.protocols
        [cljss.selectors.combination :only (combine)]))

(defn- combine-or-replace [sel parent-sel]
  (if-not (parent? sel)
    (combine parent-sel sel)
    (replace-parent sel parent-sel)))

(def combine-selector-decorator
  "This decorator is used to combine the selectors of sub rules
  with those of their ancestors."
  (d/decorator []
   (fn [{sel :selector :as r} parent-sel]
     (let [new-sel (combine-or-replace sel parent-sel)]
       (list (assoc r :selector new-sel)
             new-sel)))))


(def simplify-selectors-decorator
  (d/decorator
    (fn [{sel :selector :as r} ctxt]
      (list (assoc r 
              :selector (simplify sel))
            ctxt))))


