(ns cljss.selectors.basic
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.compilation.protocols))

(extend-type String
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this] this))

(derive String simple-t)

(extend-type clojure.lang.Keyword
  Neutral
  (neutral? [_] false)
  
  CssSelector
  (compile-as-selector [this] (name this)))

(derive clojure.lang.Keyword        simple-t)








