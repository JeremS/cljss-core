(ns cljss.selectors.combinators
  (:require [cljss.compilation.utils :as utils])
  (:use cljss.selectors.protocols
        cljss.selectors.types
        cljss.selectors.combination
        cljss.compilation.protocols
        [clojure.pprint :only (pprint)]))

(defn compile-path-sel
  "Compile a path like selector."
  [sels]
  (utils/compile-seq-then-join sels compile-as-selector \space))

(extend-type clojure.lang.PersistentVector
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (compile-path-sel this)))

(defn descendants-c [& sels]
  (vec sels))

(def descendant-t  ::descendant)
(derive descendant-t                  combination-t)
(derive clojure.lang.PersistentVector descendant-t)

(defmethod combine [descendant-t descendant-t]
  [v1 v2]
  (vec (concat v1 v2)))

;; ----------------------------------------------------------------------------
(defn compile-set-sel
  "Compile a set selector."
  [sels]
  (utils/compile-seq-then-join sels compile-as-selector ", "))

(extend-type clojure.lang.IPersistentSet
  Neutral
  (neutral? [this] (-> this seq not))
  
  CssSelector
  (compile-as-selector [this]
    (compile-set-sel this)))

(derive clojure.lang.IPersistentSet set-t)

;; ----------------------------------------------------------------------------

(defn compile-children-c [sels]
  (utils/compile-seq-then-join sels compile-as-selector " > "))

(defrecord Children [sels]
  Neutral
  (neutral? [_] (-> sels seq not))
  
  CssSelector
  (compile-as-selector [_]
    (compile-children-c sels)))

(defn children-c [& sels]
  (Children. (vec sels)))

(def c-> children-c)

(derive Children  combination-t)

;; ----------------------------------------------------------------------------

(defn compile-siblings [sels]
  (utils/compile-seq-then-join sels compile-as-selector " + "))

(defrecord Siblings [sels]
  Neutral
  (neutral? [_] (-> sels seq not))
  
  CssSelector
  (compile-as-selector [_]
    (compile-siblings sels)))

(defn siblings-c [& sels]
  (Siblings. (vec sels)))

(def c-+ siblings-c)

(derive Siblings  combination-t)

;; ----------------------------------------------------------------------------

(defn compile-gsiblings [sels]
  (utils/compile-seq-then-join sels compile-as-selector " ~ "))


(defrecord GSiblings [sels]
  Neutral
  (neutral? [_] (-> sels seq not))
  
  CssSelector
  (compile-as-selector [_]
    (compile-gsiblings sels)))

(defn gsiblings-c [& sels]
  (GSiblings. (vec sels)))

(def c-g+ gsiblings-c)

(derive GSiblings combination-t)

(comment
(defmacro defcombinator [c-name c-cstr c-symbol]
  (let [cstr-sym (-> c-name (str ".") symbol)]
  `(do
     
     (defrecord ~c-name [sels#]
       Neutral
       (neutral? [_#] (-> sels# seq not))
       
       CssSelector
       (compile-as-selector [_#]
         (utils/compile-seq-then-join sels# compile-as-selector ~c-symbol)))
     
     (defn ~c-cstr [& sels#]
       (~cstr-sym (vec sels#)))
     
     (derive ~c-name combination-t))))

(def t '(defcombinator GSiblings c-g+ " ~ "))

(-> t macroexpand-1 pprint)


)

