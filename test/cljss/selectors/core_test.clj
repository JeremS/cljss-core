(ns cljss.selectors.core-test
  (:use [cljss.selectors core protocols combination parent]
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



(facts "We can compile combined selectors"

  (fact "Compiling a path like selector give the path of the compiled selectors"
    (compile-as-selector [:div "p" :.class] {}) => "div p .class"
    (compile-as-selector '(:div "p" :.class) {}) => "div p .class"
    (compile-as-selector (map identity [:div "p" :.class]) {}) => "div p .class")

  (fact "Compiling a set of selectors give the set of the compiled selectors"
    (compile-as-selector #{:div "p"} {})
    => (some-checker "div, p"
                       "p, div"))

  (fact "We can compile a seq of children selectors"
    (compile-as-selector [:div :> :p :> :a])
    => "div > p > a")

  (fact "We can compile a seq of siblings selectors"
    (compile-as-selector [:div :+ :p :+ :a])
    => "div + p + a")

  (fact "We can compile a seq of general siblings selectors"
    (compile-as-selector [:div "~" :p "~" :a])
    => "div ~ p ~ a"))

(facts "We can compile combined simple selectors"
  (fact "Compiling a sequential selector give the compilation of its parts."
    (compile-as-selector [:div ["p" :> :.class]])
    => "div p > .class"

    (compile-as-selector '(:div ["p" :> :.class]))
    => "div p > .class")

  (fact "Compiling a set of selectors give the set of the compiled selectors"
    (compile-as-selector #{[:div :> "p"] [:.class :+ :.class2]})
    => (some-checker "div > p, .class + .class2"
                     ".class + .class2, div > p")))

(fact "Combinator containing at least 1 element are not neutral when we combine the as selectors"
  (neutral? [:div])        => falsey
  (neutral? [:div :p])     => falsey

  (neutral? #{:div})       => falsey
  (neutral? #{:div :p})    => falsey)

(fact "Combinators containing 0 element are a neutral value when it comes to combining them a selectors"
  (neutral? [])     => truthy
  (neutral? #{})    => truthy)


(facts "About simplification of vector selectors (descendant combinator)"

  (fact "Returns nil when neutral"
    (simplify []) => nil)

  (fact "returns the combination left to right if a set is present."
    (simplify [[:div :p][:a]]) => [[:div :p][:a]]
    (simplify [#{:div :p}[:a]]) => #{[:div [:a]] [:p [:a]]}))


(facts "About simplification of sets"

  (fact "Returns nil when empty"
    (simplify #{}) => nil)

  (fact "Returns the set of the simplifications"
    (simplify #{[[:div :p][:a []]] [[:div :p []][:a :span]]})
    => #{(simplify [[:div :p][:a []]])
         (simplify [[:div :p []][:a :span]])})
  (fact "When sets are inside a set the inners sets a merged with the outer one"
    (simplify #{:section #{:div :p #{:span :a}} :a})
    => #{:section :div :p :span :a}))

(facts "About simplification of selectors > + ~(children, siblings, general sibilings)"
  (simplify [:div :> :p :> :a])
  => [:div :> :p :> :a]

  (simplify [#{:div :p} :> [:a]])
  => #{(combine (combine :div :>) [:a])
       (combine (combine :p  :>) [:a])})

(fact "Sets inside sets are expanded"
  (simplify #{:div #{:p :a} :span}) => #{:div :p :a :span})

(fact "We can determine if a combination of selectors contains the parent selector"
  (parent? [:div :a]) => falsey
  (parent? [:div & :a]) => truthy

  (parent? #{:div :a}) => falsey
  (parent? #{:div & :a}) => truthy


  (parent? [:div :a]) => falsey
  (parent? [:div & :a]) => truthy

  (parent? [:section #{:div :p} :a]) => falsey
  (parent? [:section #{:div :p &} :a]) => truthy)

(fact "When we try to replace the parent selector and its not used, the same selector is returned."
  (replace-parent [:div  :a] :#parent)
  => [:div :a]

  (replace-parent #{:div  :a} :#parent)
  => #{:div :a}

  (replace-parent [:div :> :a] :#parent)
  => [:div :> :a]

  (replace-parent [:section #{:div :p } :a] :#parent)
  => [:section #{:div :p} :a])

(fact "We can replace the parent selector given a replacement"
  (replace-parent [:div & :a] :#parent)
  => [:div :#parent :a]

  (replace-parent #{:div & :a} :#parent)
  => #{:div :#parent :a}

  (replace-parent [:div :> & :> :a] :#parent)
  => [:div :> :#parent :> :a]

  (replace-parent [:section #{:div :p &} :a] :#parent)
  => [:section #{:div :p :#parent} :a])
