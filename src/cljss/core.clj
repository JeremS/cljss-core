(ns ^{:author "Jeremy Schoffen."}
  cljss.core
  (:refer-clojure :exclude (rem))
  (:require [cljss.AST]
            [cljss.parse :as parse]
            [cljss.precompilation :as pre]
            [cljss.selectors :as sel]
            [cljss.compilation :as compi]
            [clojure.string :as string]
            [potemkin :as p])
  (:use cljss.protocols))


(p/import-vars
 [cljss.AST media]
 [cljss.selectors & c-> c-+ c-g+

  att-sel

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


(def ^{:arglist '([& rules])
       :doc "Create a list of rules."}
  rules list)


(def ^{:arglist '([& rules])
       :doc "Groups a list of rules."}
  group-rules concat)

(defmacro defrules
  "Defines a named list of rules."
  [r-name & body]
  `(def ~r-name
     (rules ~@body)))

(defn css-str
  "Return the string wrapped inside a pair
  of character \".
  Useful to declare a css property as a css string."
  [s] (str \" s \"))

(defn css-comment [& c]
  (str "/* "
       (string/join \newline c)
       " */"))

(defn css-with-style
  "Compile a list of rules with a given style."
  [style & rules]
  (->> rules
       parse/parse-rules
       pre/precompile-rules
       (compi/compile-rules style)))

(defn compressed-css [& rules]
  (apply css-with-style (:compressed compi/styles) rules))

(defn compact-css [& rules]
  (apply css-with-style (:compact compi/styles) rules))

(defn css [& rules]
  (apply css-with-style (:classic compi/styles) rules))