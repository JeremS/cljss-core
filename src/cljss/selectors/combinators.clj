(ns cljss.selectors.combinators
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.selectors.combination
        cljss.compilation.protocols
        
        cljss.selectors.basic
        clojure.tools.trace))

(defn- need-combination? [sels]
  (let [simple? #(isa? % simple-t)]
    (->> sels
         (map selector-type)
         (some (complement simple?)))))


(extend-type clojure.lang.PersistentVector
  Neutral
  (neutral? [this] (-> this seq not))
  
  SimplifyAble
  (simplify [this]
    (if (neutral? this) nil
      (let [this (vec (keep simplify this)) ; simplify internals
            this (if-not (need-combination? this)            ; combine left to right if possible 
                   this 
                   (reduce #(combine %1 %2) this))]
        (if (= 1 (count this)) 
          (first this) 
          this))))
  
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
  
  SimplifyAble
  (simplify [this]
   (if (neutral? this) nil
     (let [this (set (keep simplify this))]
       (if (= 1 (count this))
         (first this)
         this))))
  
  CssSelector
  (compile-as-selector [this]
    (utils/compile-seq-then-join this compile-as-selector ", ")))

(derive clojure.lang.IPersistentSet set-t)

;; ----------------------------------------------------------------------------

(defmacro defcombinator [c-name c-cstr c-sym]
  (let [cstr-sym (-> c-name (str ".") symbol)
        c-sym (str \space c-sym \space)
        sels-sym 'sels]
  `(do
     
     (declare ~c-cstr)
     
     (defrecord ~c-name [~sels-sym]
       Neutral
       (neutral? [_#] (-> ~sels-sym seq not))
       
       SimplifyAble
       (simplify [_#]
         (apply ~c-cstr (mapv simplify ~sels-sym)))
       
       CssSelector
       (compile-as-selector [_#]
         (utils/compile-seq-then-join ~sels-sym compile-as-selector ~c-sym)))
     
     (defn ~c-cstr [& sels#]
       (case (count sels#)
         0 []
         1 (first sels#)
         (~cstr-sym (vec sels#))))
     
     (derive ~c-name combination-t))))


(defcombinator Children  c->  \>)
(defcombinator Siblings  c-+  \+)
(defcombinator GSiblings c-g+ \~)