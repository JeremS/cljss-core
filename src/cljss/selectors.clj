;; ## Selector
;; We group here the different functions
;; that form the 'selector API'.

(ns cljss.selectors
  (:refer-clojure :exclude (rem))
  (:require [cljss.selectors
               core
               parent
               pseudos
               attribute
               combination]
            [potemkin :as p]))


(p/import-vars
 [cljss.selectors.parent &]

 [cljss.selectors.attribute att-sel]

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
   before after]

 [cljss.selectors.combination combine])