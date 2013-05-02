(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.basic
  (:require [cljss.compilation.utils :as utils]
            [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.types))

(extend-type nil
  Neutral
  (neutral? [this] true)

  SimplifyAble
  (simplify [this] nil)

  Parent
  (parent? [this] false)
  (replace-parent [this replacement] this)

  CssSelector
  (compile-as-selector [this] ""))


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
  (compile-as-selector [this] (string/trim this)))

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

