(ns  ^{:author "Jeremy Schoffen."}
  cljss.properties
  (:use cljss.protocols
        [cljss.compilation :only (compile-seq-then-join)]))


(defn compile-seq-property-value
  "Compile a collection representing a property's value."
  [s]
  (compile-seq-then-join s
                         compile-as-property-value
                         \space))

;; ### Protocol implementations
;; Implementation of the compilation of keywords and strings used as property names.

(extend-protocol CssPropertyName
  clojure.lang.Keyword
  (compile-as-property-name [this] (name this)))

;; Implementation of the compilation of clojure types when used as property values.

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
    (compile-seq-property-value this))

  clojure.lang.LazySeq
  (compile-as-property-value [this]
    (compile-seq-property-value this)))

