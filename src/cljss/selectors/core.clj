;; ## Core
;; Implementation of the protocols for the core selctor dsl.

(ns cljss.selectors.core
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.protocols
        cljss.selectors.types
        cljss.selectors.combination
        [cljss.compilation :only (compile-seq-then-join)]

        clojure.tools.trace
        ))

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

(defn- set-t? [c]
  (-> c selector-type (isa? set-t)))

(defn- contains-set? [sels]
    (some set-t? sels))


(defn- seq-simplifyable? [s]
  (or (>= 1 (count s))
      (some #(or (simplifyable? %)
                 (set-t? %))
            s)))

(defn- clean-seq [s seq-cstr]
  (->> s
       (keep simplify)
       (remove neutral?)
       seq-cstr))

(defn- expand-sets [s]
  (let [simples (remove set-t? s)
        sets (filter set-t? s)
        values (apply concat sets)]
    (into (set simples) values)))

(extend-protocol SimplifyAble
  nil
  (simplifyable? [_] true)
  (simplify [_] nil)

  Object
  (simplifyable? [_] false)
  (simplify [this] this)

  String
  (simplifyable? [this]
    (neutral? this))
  (simplify [this]
    (if (neutral? this) nil this))

  clojure.lang.Keyword
  (simplifyable? [_] false)
  (simplify [this] this)


  clojure.lang.Sequential

  (simplifyable? [this]
    (seq-simplifyable? this))

  (simplify [this]
    (cond (neutral? this)            nil
          (= 1 (count this))         (simplify (first this))
          (not (simplifyable? this)) this
          :else
           (let [this (clean-seq this vec)]
             (if-not (contains-set? this)
               (simplify this)
               (simplify (reduce #(combine %1 %2) this))))))


  clojure.lang.IPersistentSet

  (simplifyable? [this]
    (seq-simplifyable? this))

  (simplify [this]
            (cond (neutral? this)            nil
                  (= 1 (count this))         (simplify (first this))
                  (not (simplifyable? this)) this
                  :else
                   (let [this (clean-seq this set)]
                     (if-not (contains-set? this)
                       (simplify this)
                       (simplify (expand-sets this)))))))

(simplify #{[[:div :p][:a []]] [[:div :p []][:a :span]]})
(simplify [#{:div :p} :> [:a]])
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

