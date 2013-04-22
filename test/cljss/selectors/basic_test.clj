(ns cljss.selectors.basic-test
  (:require [midje.repl :as m])
  (:use cljss.selectors.basic
        cljss.protocols))

(m/facts "We can compile simple selectors"
         
         (m/fact "Compiling a String selector gives the same string"
                 (compile-as-selector "div") => "div"
                 (compile-as-selector "a") => "a"
                 (compile-as-selector ".class") => ".class")
         
         (m/fact "Compiling a Keyword selector gives the name of the keyword"
                 (compile-as-selector :div) => "div"
                 (compile-as-selector :a) => "a"
                 (compile-as-selector :.class) => ".class"))

(m/fact "Keyword are not a neutral element when in comes to combine them as selectors"
        (neutral? :div) => m/falsey
        (neutral? :p)   => m/falsey)

(m/fact "Non empty string are not neitral when it comes to combine them as selector"
        (neutral? "div") => m/falsey
        (neutral? "p")   => m/falsey)

(m/fact "The empty string is neutral when it comes to combine it as a selector"
        (neutral? "")   => m/truthy)

(m/fact "Simplifying non neutral basic selectors returns the selector"
        (simplify "div") => "div"
        (simplify :div)  => :div)

(m/fact "Simplify a neutral simple selector (empty string) returns nil"
        (simplify "") => nil)

(m/fact "Basic selectors arent the parent decorator"
        (parent? "div") => m/falsey
        (parent? :div)  => m/falsey
        (parent? nil)   => m/falsey)