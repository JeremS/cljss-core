(ns cljss.selectors.combinators
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.protocols
        cljss.selectors.types
        cljss.selectors.combination
        
        clojure.tools.trace))

(defn- need-combination? [sels]
  (let [set-t? #(isa? % set-t)]
    (->> sels
         (map selector-type)
         (some set-t?))))


(extend-type clojure.lang.PersistentVector
  Neutral
  (neutral? [this] (-> this seq not))
  
  SimplifyAble
  (simplify [this]
    (if (neutral? this) nil
      (let [this (->> this
                      (keep simplify)
                      (remove neutral?)
                      (vec))] ; simplify internals
        (if-not (need-combination? this)            ; combine left to right if possible 
          this 
          (reduce #(combine %1 %2) this)))))

  Parent
  (parent? [this] (some parent? this))
  (replace-parent [this replacement]
    (->> this
         (map #(replace-parent % replacement))
         (into [])))
  
  CssSelector
  (compile-as-selector [this]
    (utils/compile-seq-then-join this compile-as-selector \space)))


(derive clojure.lang.PersistentVector  combination-t)



;; ----------------------------------------------------------------------------

(extend-type clojure.lang.IPersistentSet
  Neutral
  (neutral? [this] (-> this seq not))
  
  SimplifyAble
  (simplify [this]
   (if (neutral? this) 
     nil
     (set (keep simplify this))))
  
  Parent
  (parent? [this] (some parent? this))
  
  (replace-parent [this replacement]
    (->> this
         (map #(replace-parent % replacement))
         (into #{})))
  
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
         (let [simplification# (simplify ~sels-sym)]
           (if-not (-> simplification# selector-type (isa? set-t))
             (apply ~c-cstr simplification#)
             (->> simplification#
                  (map #(apply ~c-cstr %))
                  (into #{})))))
       
       Parent
       (parent? [_#] (some parent? ~sels-sym))
       
       (replace-parent [_# replacement#]
         (~cstr-sym (replace-parent ~sels-sym replacement#)))
       
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
