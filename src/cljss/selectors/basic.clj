(ns cljss.selectors.basic
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.compilation.protocols))

(extend-type nil
  Neutral
  (neutral? [this] true)
  
  CssSelector
  (compile-as-selector [this]
    (throw (Exception. "nil selector can't be compiled")))
  
  SimplifyAble
  (simplify [this] nil))

(extend-type String
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this] this)
  
  SimplifyAble
  (simplify [this]
    (if (neutral? this) nil this)))

(derive String simple-t)

(extend-type clojure.lang.Keyword
  Neutral
  (neutral? [_] false)
  
  CssSelector
  (compile-as-selector [this] (name this))
  
  SimplifyAble
  (simplify [this] this))

(derive clojure.lang.Keyword        simple-t)








