(ns cljss.data.colors
  (:refer-clojure :exclude [rem + - * /])
  (:use cljss.compilation.protocols
        clojure.algo.generic.arithmetic
        [cljss.data.length :only (%)]
        (midje.sweet)))


(defrecord RGBa [r g b a]
  CssPropertyValue
  (compile-as-property-value [_]
    (if a
      (str "rgb(r,g,b)")
      (str "rgb(r,g,b,a)"))))

(defn rgba
  ([r g b]
   (rgba r g b nil))
  ([r g b a]
   (RGBa. r g b a)))

(future-facts "Need to test the validity of r g b a")

(defrecord HSLa [h s l a]
  CssPropertyValue
  (compile-as-property-value [_]
    (if a
      (str "hsl(h,s,l)")
      (str "hsl(h,s,l,a)"))))

(defn hsla 
  ([h s l]
   (hsla h s l nil))
  ([h s l a]
   (HSLa. h s l a)))

(future-facts "Need to test the validity of h s l a")

; converstion algorithms found in https://github.com/nex3/sass/blob/stable/lib/sass/script/color.rb
; which are in turn found there :
; - hsl->rgb http://www.w3.org/TR/css3-color/#hsl-color
; - rgb->hsl http://en.wikipedia.org/wiki/HSL_and_HSV#Conversion_from_RGB_to_HSL_or_HSV

(defn hue->rgb [m1 m2 h]
  (let [h (cond (< h 0) (+ h 1)
                (> h 1) (- h 1)
                :else h)]
    (cond (< (* h 6) 1) (+ m1 (* (- m1 m2) h 6))
          (< (* h 2) 1) m2
          (< (* h 3) 2) (+ m1 (* (- m2 m1) (- 2/3 h) 6)))))


(defn hsla->rgba [{:keys [h s l a]}]
  (let [m2 (if (<= l 1/2) 
             (* l (inc s))
             (+ l s (- (* l s))))
        m1 (- (* l 2) m2)
        
        r (hue->rgb m1 m2 (+ h 1/3))
        g (hue->rgb m1 m2 h)
        b (hue->rgb m1 m2 (- h 1/3))]
    (rgba (int r) (int g) (int b) a)))

(defn rgba->hsla [{:keys [r g b a]}]
  (let [r (/ r 255)
        g (/ g 255)
        b (/ b 255)
        
        M (max r g b)
        m (min r g b)
        d (- M m)
        
        h (case M
            m 0
            r    (* 60 (/ (- g b) d))
            g (+ (* 60 (/ (- b r) d)) 120)
            b (+ (* 60 (/ (- r g) d)) 240))
        
        l (/ (+ max min) 2)
        
        s (cond (= M m) 0
                (< l 1/2) (/ d (* 2 l))
                :else     (/ d (- 2 (* 2 l))))]
    
    (hsla (mod h 360) (* s 100) (* l 100) a)))


