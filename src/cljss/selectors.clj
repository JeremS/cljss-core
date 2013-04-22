(ns cljss.selectors
  (:require cljss.selectors.basic
            cljss.selectors.combinators)
  (:use cljss.selectors.protocols
        [cljss.precompilation :only (decorator)]
        [cljss.selectors.combination :only (combine)]))

(defn- combine-or-replace [sel parent-sel]
  (if-not (parent? sel)
    (combine parent-sel sel)
    (replace-parent sel parent-sel)))

(def combine-or-replace-parent-decorator
  "This decorator is used to combine the selectors of sub rules
  with those of their ancestors."
  (decorator []
   (fn [{sel :selector :as r} parent-sel]
     (let [new-sel (combine-or-replace sel parent-sel)]
       (list (assoc r :selector new-sel)
             new-sel)))))


(def simplify-selectors-decorator
  (decorator
    (fn [{sel :selector :as r} ctxt]
      (list (assoc r 
              :selector (simplify sel))
            ctxt))))


