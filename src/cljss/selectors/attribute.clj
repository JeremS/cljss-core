;; ## Attribute Selectors
;; The support for attribute selectors is implemented here.

(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.attribute
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.protocols
        cljss.selectors.types))

(declare att-sel)

(defn- suppr-brackets [s]
  (string/replace s #"[\[\]]" ""))

(defn add-brackets [s]
  (str \[ s \]))


(defrecord AttributeSelector [selector selections]
  Neutral
  (neutral? [_] false)

  SimplifyAble
  (simplify [_]
    (apply att-sel (simplify selector) selections))

  Parent
  (parent? [_] (parent? selector))

  (replace-parent [_ replacement]
    (apply att-sel
           (replace-parent selector replacement)
           selections))

  CssSelector
  (compile-as-selector [_]
    (str (compile-as-selector selector)
         (->> selections
              (map (comp add-brackets string/trim suppr-brackets))
              (apply str ))))
  (compile-as-selector [this _]
    (compile-as-selector this)))

(derive AttributeSelector simple-t)



(defn att-sel [selector & selections]
  (cond
   (isa? (selector-type selector) set-t)
    (set (map #(apply att-sel % selections) selector))
   (isa? (selector-type selector) neutral-t)
     []
   :else
     (AttributeSelector. selector selections)))
