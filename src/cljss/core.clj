(ns cljss.core
  (:refer-clojure :exclude (rem))
  (:require [clojure.string :as string]
            [cljss.AST :as AST]
            [cljss.parse :as parse]
            [cljss.precompilation :as pre]
            [cljss.selectors :as sel]
            [cljss.selectors parent pseudos]
            [cljss.compilation :as compilation]
            [cljss.compilation.styles :as styles]
            [potemkin :as p])
  (:use cljss.protocols))


(p/import-vars
 [cljss.selectors.parent &]
 
 [cljss.selectors.combinators c-> c-+ c-g+ ]
 
 [cljss.selectors.pseudos
   link visited hover active focus
  
   target lang
  
   enabled disabled checked indeterminate
  
   root
   nth-child     nth-last-child
   nth-of-type   nth-last-of-type
   first-child   last-child
   first-of-type last-of-type
   only-child
  
   css-empty
   css-not
  
   first-line
   first-letter
   before after])

(defn- parse-rules [rules]
  (map parse/parse-rule rules))

(defn- precompile-rules [rules]
  (mapcat pre/precompile-rule rules))


(defn compile-css [rules {sep :rules-separator :as style}]
  (->> rules
      (map #(css-compile % style))
      (string/join sep )))

(defn css-with-style [style & rules]
  (-> rules
       parse-rules
       precompile-rules
       (compile-css style)))

(defn compressed-css [& rules]
  (apply css-with-style styles/compressed rules))

(defn css [& rules]
  (apply css-with-style styles/classic rules))
