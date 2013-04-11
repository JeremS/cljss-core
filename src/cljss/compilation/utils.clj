(ns cljss.compilation.utils
  (:require [clojure.string :as string]))

(defn compile-seq-then-join
  "Compile each value of a collection using compile-fn,
  then join the results with the string s."
  [vs compile-fn s]
  (->> vs
       (map compile-fn)
       (string/join s)))