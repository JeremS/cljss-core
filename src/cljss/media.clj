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
  (css-compile [_ {sep :rules-separator :as style}]
    (str exp \space \{
           (map #(css-compile % (assoc-in style [depth] 1)) body)
         \} sep)))


(defn media [exp & body]
  (Query. exp (vec body)))