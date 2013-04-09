(ns cljss.data.length
  (:refer-clojure :exclude [rem + - * /])
  (:use cljss.compilation.protocols
        clojure.algo.generic.arithmetic))


(defrecord Length [l u]
  CssPropertyValue
  (compile-as-property-value [_]
    (str l (name u))))


(defn make-length [l u]
  {:pre [(number? l) 
         (or (symbol? u) (keyword u) (string? u))]}
  (Length. l (keyword u)))


(defmacro def-length-ctr [unit]
  `(defn ~unit [l#]
     (make-length l# '~unit)))

(def-length-ctr em)
(def-length-ctr rem)
(def-length-ctr ex)
(def-length-ctr ch)

(def-length-ctr vw)
(def-length-ctr vh)
(def-length-ctr vmin)
(def-length-ctr vmax)

(def-length-ctr %)

(def-length-ctr px)
(def-length-ctr mm)
(def-length-ctr cm)
(def-length-ctr in)
(def-length-ctr pt)
(def-length-ctr pc)



(defn type-error [u1 u2]
  (str "Can't operate on units" u1 \space u2))

(defmethod + [Length Length]
  [{l1 :l u1 :u} {l2 :l u2 :u}]
  (assert (= u1 u2) (type-error u1 u2))
  (make-length (clojure.core/+ l1 l2) u1))


(defmethod - Length
  [{l :l u :u}]
  (make-length (clojure.core/- l) u))

(defmethod - [Length Length]
  [{l1 :l u1 :u} {l2 :l u2 :u}]
  (assert (= u1 u2) (type-error u1 u2))
  (make-length (clojure.core/- l1 l2) u1))




(defmethod * [Length Number]
  [{l :l u :u} n]
  (make-length (clojure.core/* l n) u))

(defmethod * [Number Length]
  [n {l :l u :u}]
  (make-length (clojure.core/* l n) u))

(defmethod / [Length Number]
  [{l :l u :u} n]
  (make-length (clojure.core// l n) u))

