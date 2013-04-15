(ns cljss.selectors.simple-selectors-test
  (:require [midje.sweet :as m])
  (:use cljss.selectors.simple-selectors
        cljss.compilation.protocols))


(m/facts "We can compile simple selectors"
         
         (m/fact "Compiling a String selector gives the same string"
                 (compile-as-selector "div") => "div"
                 (compile-as-selector "a") => "a"
                 (compile-as-selector ".class") => ".class")
         
         (m/fact "Compiling a Keyword selector gives the name of the keyword"
                 (compile-as-selector :div) => "div"
                 (compile-as-selector :a) => "a"
                 (compile-as-selector :.class) => ".class"))


(m/facts "We can compile combined selectors"
         
         (m/fact "Compiling a path like selector give the path of the compiled selectors"
                 (compile-as-selector [:div "p" :.class]) => "div p .class")
         
         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector #{:div "p"}) 
                 => (m/some-checker "div, p"
                                    "p, div"))
         
         (m/fact "We can compile a seq of children selectors"
                 (compile-as-selector (c-> :div :p :a)) 
                 => "div > p > a")
         
         (m/fact "We can compile a seq of siblings selectors"
                 (compile-as-selector (c-+ :div :p :a)) 
                 => "div + p + a")
         
         (m/fact "We can compile a seq of general siblings selectors"
                 (compile-as-selector (c-g+ :div :p :a)) 
                 => "div ~ p ~ a"))

(m/facts "We can compile combined simple selectors"
         
         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector [:div (children-c "p" :.class)]) 
                 => "div p > .class")
         
         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector (children-c :div ["p" :.class])) 
                 => "div > p .class")
         
         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector (children-c :div ["p" (siblings-c :.class :.class2)])) 
                 => "div > p .class + .class2"))
