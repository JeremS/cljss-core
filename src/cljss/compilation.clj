(ns ^{:author "Jeremy Schoffen."}
  cljss.compilation
  (:require cljss.AST
            [clojure.string :as string])
  (:use cljss.protocols
        cljss.compilation.utils)
  (:import [cljss.AST InlineCss]))


(defn compile-seq-property-value
  "Compile a collection representing a property's value."
  [s]
  (compile-seq-then-join s
                         compile-as-property-value
                         \space))


(extend-protocol CssPropertyName
  clojure.lang.Keyword
  (compile-as-property-name [this] (name this))

  String
  (compile-as-property-name [this] this))



(extend-protocol CssPropertyValue
  String
  (compile-as-property-value [this] this)

  Number
  (compile-as-property-value [this] (str this))

  clojure.lang.Keyword
  (compile-as-property-value [this] (name this))

  clojure.lang.PersistentVector
  (compile-as-property-value [this]
    (compile-seq-property-value this))

  clojure.lang.PersistentList
  (compile-as-property-value [this]
    (compile-seq-property-value this)))


(defn compile-rule [style rule]
  (css-compile rule style))


(defn compile-rules [{sep :rules-separator :as style} rules]
  (->> rules
       (remove empty-rule?)
       (map (partial compile-rule style))
       (string/join sep )))



(def styles
  "Styles used when compiling rules. A style is a map,
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