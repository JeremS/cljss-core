;; ## Combinators
;; Implementations of what we could call the verbs
;; of the selector DSL.

(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.combinators
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.types
        cljss.selectors.combination
        [cljss.compilation :only (compile-seq-then-join)]))

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
    (let [break (:selector-break style)
          indent (:outer-indent style)]
      (if (and break (pos? break))
        (->> this
             (map compile-as-selector)
             (interpose ", ")
             (partition-all (* 2 break))
             (map #(apply str %))
             (string/join (str \newline indent)))
        (compile-as-selector this))))))


(derive clojure.lang.IPersistentSet set-t)