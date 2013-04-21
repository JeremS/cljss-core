(ns cljss.selectors.basic
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.compilation.protocols))

(extend-type nil
  Neutral
  (neutral? [this] true)
  
  SimplifyAble
  (simplify [this] nil)
  
  Parent
  (parent? [this] false)
  (replace-parent [this replacement] this)
  
  CssSelector
  (compile-as-selector [this]
    (throw (Exception. "nil selector can't be compiled"))))


(extend-type String
  Neutral
  (neutral? [this] (-> this seq not))
  
  SimplifyAble
  (simplify [this]
    (if (neutral? this) nil this))
  
  Parent
  (parent? [this] false)
  (replace-parent [this replacement] this)
  
  CssSelector
  (compile-as-selector [this] this))

(derive String simple-t)


(extend-type clojure.lang.Keyword
  Neutral
  (neutral? [_] false)
  
  SimplifyAble
  (simplify [this] this)
  
  Parent
  (parent? [this] false)
  (replace-parent [this replacement] this)
  
  CssSelector
  (compile-as-selector [this] (name this)))

(derive clojure.lang.Keyword        simple-t)

