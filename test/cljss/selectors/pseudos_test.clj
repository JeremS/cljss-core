(ns cljss.selectors.pseudos-test
  (:require [midje.repl :as m])
  (:use cljss.selectors.pseudos
        cljss.selectors.basic
        cljss.selectors.combinators
        cljss.protocols
        [cljss.selectors.parent :only (&)]))

(m/fact "We can add pseudo classes and pseudo elements to simple selectors"
        (-> "div" hover compile-as-selector) => "div:hover"
        (-> :div hover compile-as-selector)  => "div:hover"
        
        (-> "div" (nth-child "2n+1") compile-as-selector) => "div:nth-child(2n+1)")

(m/fact "We can add more tah one pseudo classe / element"
        (-> :div hover first-line compile-as-selector) => "div:hover::first-line")

(m/fact "We can use pseudo classes on combined selectors"
        (-> [:#id :a] hover compile-as-selector) => "#id a:hover"
        (-> (c-> :#id :p :a) hover compile-as-selector) => "#id > p > a:hover"
        (-> #{:#id :a} hover compile-as-selector) => (m/some-checker "#id:hover, a:hover"
                                                                     "a:hover, #id:hover"))

(m/fact "Simplify a pseudo classed element gives the pseudo class of the simplifiction"
        (-> [:section #{:div :p} :span] hover simplify) 
        => (-> [:section #{:div :p} :span] simplify hover))

(m/fact "We can test for parent use inside pseudos"
        (-> [:section #{:div :p} :span] hover parent?) => m/falsey
        (-> [:section #{:div &} :span] hover parent?) => m/truthy)

(m/fact "We can replace the parent selector"
        (-> [:section #{:div &} :span] hover (replace-parent :#parent))
        => (-> [:section #{:div :#parent} :span] hover))
