;; ## Properties
;; We group here the necessary code to handle
;; css properties

(ns cljss.properties
  (:use cljss.protocols
        [cljss.compilation :only (compile-seq-then-join)]))


;; ### Protocol implementations
;; Implementation of the compilation of keywords used as property names.

(extend-protocol CssPropertyName
  clojure.lang.Keyword
  (compile-as-property-name [this] (name this)))


;; Implementation of the compilation of clojure types when used as property values.

(defn compile-seq-property-value
  "Compile a collection representing a property's value."
  [s]
  (compile-seq-then-join s
                         compile-as-property-value
                         \space))

(extend-protocol CssPropertyValue
  Object
  (compile-as-property-value [this] (str this))

  String
  (compile-as-property-value [this] this)

  clojure.lang.Keyword
  (compile-as-property-value [this] (name this))

  clojure.lang.Sequential
  (compile-as-property-value [this]
    (compile-seq-property-value this))

  clojure.lang.IPersistentSet
  (compile-as-property-value [this]
    (compile-seq-then-join this compile-as-property-value ", ")))

