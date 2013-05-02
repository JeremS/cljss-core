(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.combinators-test
  (:require [midje.repl :as m])
  (:use cljss.selectors.basic
        cljss.selectors.combinators
        cljss.selectors.combination
        cljss.protocols
        [cljss.selectors.parent :only (&)]))

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
                 (compile-as-selector [:div (c-> "p" :.class)])
                 => "div p > .class")

         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector (c-> :div ["p" :.class]))
                 => "div > p .class")

         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector (c-> :div ["p" (c-+ :.class :.class2)]))
                 => "div > p .class + .class2"))

(m/fact "Combinator containing at least 1 element are not neutral when we combine the as selectors"
        (neutral? [:div])        => m/falsey
        (neutral? [:div :p])     => m/falsey

        (neutral? #{:div})       => m/falsey
        (neutral? #{:div :p})    => m/falsey

        (neutral? (c-> :div))    => m/falsey
        (neutral? (c-> :div :p)) => m/falsey

        (neutral? (c-+ :div))    => m/falsey
        (neutral? (c-+ :div :p)) => m/falsey

        (neutral? (c-g+ :div))    => m/falsey
        (neutral? (c-g+ :div :p)) => m/falsey)

(m/fact "Combinators containing 0 element are a neutral value when it comes to combining them a selectors"
        (neutral? [])     => m/truthy

        (neutral? #{})    => m/truthy

        (neutral? (c-> )) => m/truthy

        (neutral? (c-+ )) => m/truthy

        (neutral? (c-g+)) => m/truthy)


(m/facts "About simplification of vector selectors (descendant combinator)"

         (m/fact "Returns nil when neutral"
                 (simplify []) => nil)

         (m/fact "returns the combination left to right if a set is present."
                 (simplify [[:div :p][:a]]) => [[:div :p][:a]]
                 (simplify [#{:div :p}[:a]]) => #{[:div [:a]] [:p [:a]]}))

(m/facts "About simplification of sets"

         (m/fact "Returns nil when empty"
                 (simplify #{}) => nil)

         (m/fact "Returns the set of the simplifications"
                 (simplify #{[[:div :p][:a []]] [[:div :p []][:a :span]]})
                 => #{(simplify [[:div :p][:a []]])
                      (simplify [[:div :p []][:a :span]])})
         (m/fact "When sets are inside a set the inners sets a merged with the outer one"
                 (simplify #{:section #{:div :p #{:span :a}} :a})
                 => #{:section :div :p :span :a}))

(m/facts "About simplification of selectors > + ~(children, siblings, general sibilings)"

         (m/fact "Returns nil when neutral"
                 (simplify (c-> )) => nil)

         (m/fact "Returns nil when empty"
                 (simplify (c-> )) => nil
                 (simplify (c-+ )) => nil
                 (simplify (c-g+)) => nil)

         (m/fact "it simplifies inside"
                 (simplify (c-> (c-+ :div :p)[:a])) => (c-> (c-+ :div :p)[:a])
                 (simplify (c-> #{:div :p}[:a])) => #{(apply c-> (combine :div [:a]))
                                                      (apply c-> (combine :p [:a]))}))

(m/fact ""
  (simplify #{:div #{:p :a} :span}) => #{:div :p :a :span})

(m/fact "We can determine if a combination of selectors contains the parent selector"
        (parent? [:div :a]) => m/falsey
        (parent? [:div & :a]) => m/truthy

        (parent? #{:div :a}) => m/falsey
        (parent? #{:div & :a}) => m/truthy


        (parent? (c-> :div :a)) => m/falsey
        (parent? (c-> :div & :a)) => m/truthy

        (parent? [:section #{:div :p} :a]) => m/falsey
        (parent? [:section #{:div :p &} :a]) => m/truthy)

(m/fact "When we try to replace the parent selector and its not used, the same selector is returned."
        (replace-parent [:div  :a] :#parent)
        => [:div :a]

        (replace-parent #{:div  :a} :#parent)
        => #{:div :a}

        (replace-parent (c-> :div :a) :#parent)
        => (c-> :div :a)

        (replace-parent [:section #{:div :p } :a] :#parent)
        => [:section #{:div :p} :a])

(m/fact "We can replace the parent selector given a replacement"
        (replace-parent [:div & :a] :#parent)
        => [:div :#parent :a]

        (replace-parent #{:div & :a} :#parent)
        => #{:div :#parent :a}

        (replace-parent (c-> :div & :a) :#parent)
        => (c-> :div :#parent :a)

        (replace-parent [:section #{:div :p &} :a] :#parent)
        => [:section #{:div :p :#parent} :a])
