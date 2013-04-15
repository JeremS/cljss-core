(ns cljss.selectors.simple-selectors
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.compilation.protocols))

;; Compilation of simple selectors.
(extend-protocol CssSelector
  String
  (compile-as-selector [this] this)
  
  clojure.lang.Keyword
  (compile-as-selector [this] (name this)))


;; ----------------------------------------------------------------------------

(defn compile-path-sel
  "Compile a path like selector."
  [sels]
  (utils/compile-seq-then-join sels compile-as-selector \space))

(extend-type clojure.lang.PersistentVector
  CssSelector
  (compile-as-selector [this]
    (compile-path-sel this)))

(defn descendants-c [& sels]
  (vec sels))


;; ----------------------------------------------------------------------------

(defn compile-children-c [sels]
  (utils/compile-seq-then-join sels compile-as-selector " > "))

(defrecord Children [sels]
  CssSelector
  (compile-as-selector [_]
    (compile-children-c sels)))

(defn children-c [& sels]
  (Children. (vec sels)))

(def c-> children-c)

;; ----------------------------------------------------------------------------

(defn compile-siblings [sels]
  (utils/compile-seq-then-join sels compile-as-selector " + "))

(defrecord Siblings [sels]
  CssSelector
  (compile-as-selector [_]
    (compile-siblings sels)))

(defn siblings-c [& sels]
  (Siblings. (vec sels)))

(def c-+ siblings-c)

;; ----------------------------------------------------------------------------

(defn compile-gsiblings [sels]
  (utils/compile-seq-then-join sels compile-as-selector " ~ "))


(defrecord GSiblings [sels]
  CssSelector
  (compile-as-selector [_]
    (compile-gsiblings sels)))

(defn gsiblings-c [& sels]
  (GSiblings. (vec sels)))

(def c-g+ gsiblings-c)
;; ----------------------------------------------------------------------------


(defn compile-set-sel
  "Compile a set selector."
  [sels]
  (utils/compile-seq-then-join sels compile-as-selector ", "))

(extend-type clojure.lang.IPersistentSet
  CssSelector
  (compile-as-selector [this]
    (compile-set-sel this)))

