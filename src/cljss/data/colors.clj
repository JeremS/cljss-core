(ns cljss.data.colors
  (:refer-clojure :exclude [rem + - * /])
  (:use cljss.compilation.protocols
        [cljss.data.length :only (%)]))


(defrecord RGBa [r g b a]
  CssValueProperty
  (compile-as-property-value [_]
    (if (= a 1)
      (str "rgb(r,g,b)")
      (str "rgb(r,g,b,a)"))))

(def rgba
  ([r g b]
   (rgba r g b 1))
  ([r g b a]
   (RGBa. r g b a)))


(defrecord HSLa [h s l a]
  CssValueProperty
  (compile-as-property-value [_]
    (if (= a 1)
      (str "hsl(h,s,l)")
      (str "hsl(h,s,l,a)"))))

(def hsla 
  ([h s l]
   (hsla h s l 1))
  ([h s l a]
   (HSLa. h s l a)))