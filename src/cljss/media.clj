(ns cljss.media
  (:use cljss.protocols
        [cljss.rule :only (parse-rule)]
        [cljss.compilation :only (depth)]))


(defrecord Query [exp body]
  ParseAble
  (parse [this]
    (Query. exp (map parse-rule body)))
  
  DecorAble
  (decorate [_ d]
    (Query. exp (mapv #(decorate % d) body)))
  
  CSS
  (css-compile [this {sep :rules-separator :as style}]
    (let [d (or (depth this) 0)]
      (str exp \space \{
             (map #(css-compile % (assoc style depth d)) body)
           \} sep))))


(defn media [exp & body]
  (Query. exp (vec body)))