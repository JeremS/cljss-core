(ns cljss.core
  (:refer-clojure :exclude (rem))
  (:require [cljss.AST]
            [cljss.parse :as parse]
            [cljss.precompilation :as pre]
            [cljss.selectors :as sel]
            [cljss.compilation :as compi]
            [cljss.compilation.styles :as styles]
            [potemkin :as p])
  (:use cljss.protocols))


(p/import-vars
 [cljss.selectors & c-> c-+ c-g+
  
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




(defn css-with-style [style & rules]
  (->> rules
       parse/parse-rules
       pre/precompile-rules
       (compi/compile-css style)))

(defn compressed-css [& rules]
  (apply css-with-style styles/compressed rules))

(defn css [& rules]
  (apply css-with-style styles/classic rules))
