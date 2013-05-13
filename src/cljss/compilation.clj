;; ## Compilation
;; We handle in this namespace the 'high level API'
;; regarding compilation of an AST.

(ns ^{:author "Jeremy Schoffen."}
  cljss.compilation
  (:require [clojure.string :as string])
  (:use cljss.protocols))


(defn compile-seq-then-join
  "Compile each value of a collection using compile-fn,
  then join the results with the string s."
  [values compile-fn separator]
  (->> values
       (map compile-fn)
       (string/join separator)))


;; ### General API
;; We handle here the compilation of an AST.

(defn compile-rule [style rule]
  (css-compile rule style))


(defn compile-rules [{sep :rules-separator :as style} rules]
  (->> rules
       (remove empty-rule?)
       (map (partial compile-rule style))
       (string/join sep )))


;; ### Styles

(def styles
  "Clasic styles used when compiling rules. A style is a map,
  keys represent output option.

   - :indent-unit number of character used to indent
   - :property-separator string put at the end of a css property declaration
   - :rules-separator string put at the end of a rule declaration
   - :start-properties string put just after the opening bracket of a cs rule declaration
   - :selector-break when the selector of a rule is a set, gives the number of selectors
     before a line break
   - :comments boolean value to indicate if comment are part of the output css"
  {:compressed
   {:indent-unit ""
    :property-separator ""
    :rules-separator ""
    :start-properties ""
    :comments false}

   :compact
   {:indent-unit ""
    :selector-break 3
    :property-separator ""
    :rules-separator \newline
    :start-properties ""
    :comments true}

   :classic
   {:indent-unit "  "
    :selector-break 1
    :property-separator \newline
    :rules-separator \newline
    :start-properties \newline
    :comments true}})