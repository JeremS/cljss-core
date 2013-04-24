(ns cljss.core
  (:require [clojure.string :as string]
            [potemkin :as p]
            [cljss.selectors parent pseudos])
  (:use clojure.tools.trace
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation :only (chain-decorators precompile-rule)]
        [cljss.selectors :only (combine-or-replace-parent-decorator
                                simplify-selectors-decorator)]
        [cljss.compilation :only (depth-decorator
                                  compile-rules)]
        [cljss.compilation.styles :only (compressed-style classic-style)]))

(def default-decorator
  (chain-decorators combine-or-replace-parent-decorator
                    simplify-selectors-decorator
                    depth-decorator))

(defn- parse-rules [rules]
  (map parse-rule rules))

(defn- precompile-rules [rules]
  (mapcat #(precompile-rule % default-decorator) rules))



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

(defn css-with-style [style & rules]
  (-> rules
       parse-rules
       precompile-rules
       (compile-rules style)))


(defn compressed-css [& rules]
  (apply css-with-style compressed-style rules))

(defn css [& rules]
  (apply css-with-style classic-style rules))
