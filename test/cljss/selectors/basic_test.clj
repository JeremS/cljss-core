(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.basic-test
  (:use cljss.selectors.basic
        cljss.protocols
        midje.sweet))

(facts "We can compile simple selectors"
  (fact "Compiling a String selector gives the same string"
    (compile-as-selector "div") => "div"
    (compile-as-selector "a") => "a"
    (compile-as-selector ".class") => ".class"
    (compile-as-selector ".class" {}) => ".class")

  (fact "Compiling a Keyword selector gives the name of the keyword"
    (compile-as-selector :div) => "div"
    (compile-as-selector :a) => "a"
    (compile-as-selector :.class) => ".class"
    (compile-as-selector :.class {}) => ".class"))

(fact "Keyword are not a neutral element when in comes to combine them as selectors"
  (neutral? :div) => falsey
  (neutral? :p)   => falsey)

(fact "Non empty string are not neitral when it comes to combine them as selector"
  (neutral? "div") => falsey
  (neutral? "p")   => falsey)

(fact "The empty string is neutral when it comes to combine it as a selector"
  (neutral? "")   => truthy)

(fact "Simplifying non neutral basic selectors returns the selector"
  (simplify "div") => "div"
  (simplify :div)  => :div)

(fact "Simplify a neutral simple selector (empty string) returns nil"
  (simplify "") => nil)

(fact "Basic selectors arent the parent decorator"
  (parent? "div") => falsey
  (parent? :div)  => falsey
  (parent? nil)   => falsey)