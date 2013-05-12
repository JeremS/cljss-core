(ns cljss.functions
  (:require [clojure.string :as string])
  (:use cljss.protocols))

(defrecord CssFunction [name args]
  CssPropertyValue
  (compile-as-property-value [_]
    (str (compile-as-property-value name)
         \( (->> args
                 (map compile-as-property-value)
                 (string/join ", "))
         \))))


(defn url [& u]
  (CssFunction. "url" u))

(compile-as-property-value (url "http://www.toto.com" :titi))