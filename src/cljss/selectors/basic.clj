(ns cljss.selectors.basic
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.compilation.protocols))

(extend-type String
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this] this))

;; Compilation of simple selectors.
(extend-type clojure.lang.Keyword
  Neutral
  (neutral? [_] false)
  
  CssSelector
  (compile-as-selector [this] (name this)))


;; ----------------------------------------------------------------------------

(defn compile-path-sel
  "Compile a path like selector."
  [sels]
  (utils/compile-seq-then-join sels compile-as-selector \space))

(extend-type clojure.lang.PersistentVector
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (compile-path-sel this)))

(defn descendants-c [& sels]
  (vec sels))


;; ----------------------------------------------------------------------------

(defn compile-children-c [sels]
  (utils/compile-seq-then-join sels compile-as-selector " > "))

(defrecord Children [sels]
  Neutral
  (neutral? [_] (-> sels seq not))
  
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
  Neutral
  (neutral? [_] (-> sels seq not))
  
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
  Neutral
  (neutral? [_] (-> sels seq not))
  
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
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (compile-set-sel this)))

