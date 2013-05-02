(ns ^{:author "Jeremy Schoffen."}
  cljss.compilation.utils
  (:require [clojure.string :as string]))

(defn compile-seq-then-join
  "Compile each value of a collection using compile-fn,
  then join the results with the string s."
  [values compile-fn separator]
  (->> values
       (map compile-fn)
       (string/join separator)))