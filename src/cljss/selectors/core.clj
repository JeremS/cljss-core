;; ## Core
;; Implementation of the protocols for the core selctor dsl.

(ns cljss.selectors.core
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.protocols
        cljss.selectors.types
        cljss.selectors.combination
        [cljss.compilation :only (compile-seq-then-join)]))

(derive String simple-t)
(derive clojure.lang.Keyword        simple-t)
(derive clojure.lang.Sequential  simple-t)
(derive clojure.lang.IPersistentSet set-t)


(extend-protocol Neutral
  nil
  (neutral? [_] true)

  Object
  (neutral? [this] false)

  String
  (neutral? [this] (-> this seq not))

  clojure.lang.Keyword
  (neutral? [_] false)

  clojure.lang.Seqable
  (neutral? [this] (empty? this)))


(defn- contains-set? [sels]
  (let [set-t? #(isa? % set-t)]
    (->> sels
         (map selector-type)
         (some set-t?))))


(extend-protocol SimplifyAble
  nil
  (simplify [_] nil)

  Object
  (simplify [this] this)

  String
  (simplify [this]
    (if (neutral? this) nil this))

  clojure.lang.Keyword
  (simplify [this] this)

  clojure.lang.Sequential
  (simplify [this]
    (if (neutral? this) nil
      (let [this (->> this
                      (keep simplify)
                      (remove neutral?)
                      (vec))] ; simplify internals
        (if-not (contains-set? this)            ; combine left to right if possible
          this
          (reduce #(combine %1 %2) this)))))

  clojure.lang.IPersistentSet
  (simplify [this]
   (if (neutral? this) nil
     (let [this (set (keep simplify this))]
       (if-not (contains-set? this)
         this
         (let [simples (remove #(-> % selector-type (isa? set-t)) this)
               sets (apply concat (filter #(-> % selector-type (isa? set-t)) this))]
           (into (set simples) sets)))))))

(extend-protocol CssSelector
  nil
  (compile-as-selector
   ([this] "")
   ([this _] (compile-as-selector this)))

  Number
  (compile-as-selector
   ([this] (str this))
   ([this _]
    (compile-as-selector this)))

  String
  (compile-as-selector
   ([this] (string/trim this))
   ([this _]
    (compile-as-selector this)))

  clojure.lang.Keyword
  (compile-as-selector
   ([this] (name this))
   ([this _]
    (compile-as-selector this)))

  clojure.lang.Sequential
  (compile-as-selector
   ([this]
    (compile-seq-then-join this compile-as-selector \space))
   ([this _]
    (compile-as-selector this)))

  clojure.lang.IPersistentSet
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


(extend-protocol Parent
  nil
  (parent? [this] false)
  (replace-parent [this replacement] this)

  Object
  (parent? [this] false)
  (replace-parent [this replacement] this)

  clojure.lang.Sequential
  (parent? [this]
    (some parent? this))
  (replace-parent [this replacement]
    (map #(replace-parent % replacement) this))

  clojure.lang.IPersistentSet
  (parent? [this]
    (some parent? this))
  (replace-parent [this replacement]
    (set (map #(replace-parent % replacement)this))))

