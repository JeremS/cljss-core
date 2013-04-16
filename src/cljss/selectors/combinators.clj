(ns cljss.selectors.combinators
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.selectors.combination
        cljss.compilation.protocols
        [clojure.pprint :only (pprint)]))

(extend-type clojure.lang.PersistentVector
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (utils/compile-seq-then-join this compile-as-selector \space)))

(def descendant-t  ::descendant)
(derive descendant-t  combination-t)
(derive clojure.lang.PersistentVector descendant-t)

(defmethod combine [descendant-t descendant-t]
  [v1 v2]
  (vec (concat v1 v2)))

;; ----------------------------------------------------------------------------

(extend-type clojure.lang.IPersistentSet
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (utils/compile-seq-then-join this compile-as-selector ", ")))

(derive clojure.lang.IPersistentSet set-t)

;; ----------------------------------------------------------------------------

(defmacro defcombinator [c-name c-cstr c-sym]
  (let [cstr-sym (-> c-name (str ".") symbol)
        c-sym (str \space c-sym \space)]
  `(do
     
     (defrecord ~c-name [sels#]
       Neutral
       (neutral? [_#] (-> sels# seq not))
       
       CssSelector
       (compile-as-selector [_#]
         (utils/compile-seq-then-join sels# compile-as-selector ~c-sym)))
     
     (defn ~c-cstr [& sels#]
       (~cstr-sym (vec sels#)))
     
     (derive ~c-name combination-t))))


(defcombinator Children  c->  \>)
(defcombinator Siblings  c-+  \+)
(defcombinator GSiblings c-g+ \~)



