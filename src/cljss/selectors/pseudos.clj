(ns cljss.selectors.pseudos
  (:refer-clojure :exclude [empty not])
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.types))


(declare pseudo)

(defrecord Pseudo [selector name args]
  Neutral
  (neutral? [_] false)
  
  SimplifyAble
  (simplify [_]
   (pseudo (simplify selector) name args))
  
  Parent
  (parent? [_] (parent? selector))
  
  (replace-parent [_ replacement]
    (pseudo (replace-parent selector replacement) 
            name 
            args))
  
  CssSelector
  (compile-as-selector [_]
   (str (compile-as-selector selector) name 
        (if-not (seq args)
          ""
          (str \( (string/join \, args) \))))))


(derive Pseudo simple-t)

(defn pseudo 
  ([selector name] 
   (pseudo selector name nil))
  ([selector name args]
   (cond 
    (isa? (selector-type selector) set-t) 
      (set (map #(pseudo % name args) selector))
    
    (isa? (selector-type selector) neutral-t)
      []
    
    :else 
      (Pseudo. selector name args))))



(defmacro defpseudo [ps-name ps-prefix]
  (let [ps-compilation (str ps-prefix ps-name)]
    `(defn ~ps-name
       ([]
        (pseudo :* ~ps-compilation))
       ([sel#]
        (pseudo sel# ~ps-compilation))
       ([sel# & args#]
        (pseudo sel# ~ps-compilation args#)))))


(defmacro defpseudo-classes [& classes-names]
  (cons 'do (for [n classes-names]
              `(defpseudo ~n \:))))

(defmacro defpseudo-elements [& classes-names]
  (cons 'do (for [n classes-names]
              `(defpseudo ~n "::"))))


(defpseudo-classes 
  ;dynamic pseudo classes
  link visited ; link 
  hover active focus ; user action
  
  target
  
  lang
  
  ;ui element states pseudo classes
  enabled disabled
  checked
  indeterminate
  
  ;structural pseudo classes
  root
  nth-child     nth-last-child
  nth-of-type   nth-last-of-type
  first-child   last-child
  first-of-type last-of-type
  only-child
  empty
  not)

(defpseudo-elements
  first-line
  first-letter
  before after)

