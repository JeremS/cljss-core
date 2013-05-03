(ns ^{:author "Jeremy Schoffen."}
  cljss.compilation
  (:require cljss.AST
            [clojure.string :as string])
  (:use cljss.protocols
        cljss.compilation.utils))


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
       (map (partial compile-rule style))
       (string/join sep )))



(def styles
  {:compressed
   {:indent-unit ""
    :property-separator ""
    :rules-separator ""
    :start-properties ""}

   :compact
   {:indent-unit ""
    :property-separator ""
    :rules-separator \newline
    :start-properties ""}

   :classic
   {:indent-unit "  "
    :property-separator \newline
    :rules-separator \newline
    :start-properties \newline}})