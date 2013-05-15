;; ## Protocols
;; General protocols used to extend cljss.

(ns cljss.protocols)


(defprotocol CSS
  (css-compile [this style]
    "Compile as a css element.")
  (empty-rule? [this]
    "True if the rule can be considered empty"))

(defprotocol CssSelector
  (compile-as-selector [this] [this style]
    "Compile a value considered a selector to a string using a style."))

(defprotocol CssPropertyName
  (compile-as-property-name [this]
    "Compile a value considered a property name to a string."))

(defprotocol CssPropertyValue
  (compile-as-property-value [this]
    "Compile a value considered a property value to a string."))