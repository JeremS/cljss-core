;; ## Combinators
;; Implementations of what we could call the verbs
;; of the selector DSL.

(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.combinators
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.types
        cljss.selectors.combination
        [cljss.compilation :only (compile-seq-then-join)]

        clojure.set
        ))

(defn- contains-set? [sels]
  (let [set-t? #(isa? % set-t)]
    (->> sels
         (map selector-type)
         (some set-t?))))


(extend-type clojure.lang.Sequential
  Neutral
  (neutral? [this] (-> this seq not))

  SimplifyAble
  (simplify [this]
    (if (neutral? this) nil
      (let [this (->> this
                      (keep simplify)
                      (remove neutral?)
                      (vec))] ; simplify internals
        (if-not (contains-set? this)            ; combine left to right if possible
          this
          (reduce #(combine %1 %2) this)))))

  Parent
  (parent? [this] (some parent? this))
  (replace-parent [this replacement]
    (->> this
         (map #(replace-parent % replacement))
         (into [])))

  CssSelector
  (compile-as-selector
   ([this]
    (compile-seq-then-join this compile-as-selector \space))
   ([this _]
    (compile-as-selector this))))


(derive clojure.lang.Sequential  combination-t)


;; ----------------------------------------------------------------------------

(extend-type clojure.lang.IPersistentSet
  Neutral
  (neutral? [this] (-> this seq not))

  SimplifyAble
  (simplify [this]
   (if (neutral? this) nil
     (let [this (set (keep simplify this))]
       (if-not (contains-set? this)
         this
         (let [simples (remove #(-> % selector-type (isa? set-t)) this)
               sets (apply concat (filter #(-> % selector-type (isa? set-t)) this))]
           (into (set simples) sets))))))

  Parent
  (parent? [this] (some parent? this))

  (replace-parent [this replacement]
    (->> this
         (map #(replace-parent % replacement))
         (into #{})))

  CssSelector
  (compile-as-selector
   ([this]
    (compile-seq-then-join this compile-as-selector ", "))
   ([this style]
    (if-let [break (:selector-break style)]
      (->> this
           (map compile-as-selector)
           (interpose ", ")
           (partition-all (* 2 break))
           (map #(apply str %))
           (string/join \newline))
      (compile-as-selector this)))))


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
         (compile-seq-then-join ~sels-sym compile-as-selector ~c-sym))
       (compile-as-selector [~'this _#]
         (compile-as-selector ~'this)))

     (defn ~c-cstr [& sels#]
       (case (count sels#)
         0 []
         1 (first sels#)
         (~cstr-sym (vec sels#))))

     (derive ~c-name combination-t))))



(defcombinator Children  c->  \>)
(defcombinator Siblings  c-+  \+)
(defcombinator GSiblings c-g+ \~)
