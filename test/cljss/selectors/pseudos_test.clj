(ns cljss.selectors.pseudos-test
  (:use cljss.selectors.pseudos
        cljss.selectors.core
        cljss.protocols
        cljss.selectors.protocols
        [midje.sweet :only (fact facts some-checker falsey truthy)]
        [cljss.selectors.parent :only (&)]))

(fact "We can add pseudo classes and pseudo elements to simple selectors"
  (-> "div" hover compile-as-selector) => "div:hover"
  (-> "div" hover (compile-as-selector {})) => "div:hover"
  (-> :div hover compile-as-selector)  => "div:hover"
  (-> "div" (nth-child "2n+1") compile-as-selector) => "div:nth-child(2n+1)"
  (-> "div" (nth-child :even) compile-as-selector) => "div:nth-child(even)"
  (-> "div" (nth-child 2) compile-as-selector) => "div:nth-child(2)")

(fact "We can add more tah one pseudo classe / element"
  (-> :div hover first-line compile-as-selector) => "div:hover::first-line")

(fact "We can use pseudo classes on combined selectors"
  (-> [:#id :a] hover compile-as-selector) => "#id a:hover"
  (-> [:#id :> :p :> :a] hover compile-as-selector) => "#id > p > a:hover"
  (-> #{:#id :a} hover compile-as-selector) => (some-checker "#id:hover, a:hover"
                                                                     "a:hover, #id:hover"))

(fact "Simplify a pseudo classed element gives the pseudo class of the simplifiction"
  (-> [:section #{:div :p} :span] hover simplify)
  => (-> [:section #{:div :p} :span] simplify hover))

(fact "We can test for parent use inside pseudos"
  (-> [:section #{:div :p} :span] hover parent?) => falsey
  (-> [:section #{:div &} :span] hover parent?) => truthy)

(fact "We can replace the parent selector"
  (-> [:section #{:div &} :span] hover (replace-parent :#parent))
  => (-> [:section #{:div :#parent} :span] hover))
