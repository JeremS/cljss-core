(ns ^{:author "Jeremy Schoffen."}
  cljss.properties-test
  (:use cljss.properties
        cljss.protocols
        midje.sweet))

(facts "About compiling css property names"
  (fact "it compiles a string to the same string"
    (compile-as-property-name "color" => "color"))

  (fact "it compiles a keyword to its name string"
          (compile-as-property-name :color) => "color"))


(facts "About compiling css property values"
  (fact "it compiles a string to the same string"
    (compile-as-property-value "black" => "black"))

  (fact "it compiles a keyword to its name string"
    (compile-as-property-value :black) => "black")

  (fact "it compiles sequentials to the space separated compilation of their values"
    (compile-as-property-value ["1px" :solid :black]) => "1px solid black"
    (compile-as-property-value '("1px" :solid :black)) => "1px solid black"
    (compile-as-property-value (map identity ["1px" :solid :black])) => "1px solid black")

  (fact "it compiles sets to the comma separated compilation of its values")
  (compile-as-property-value #{[0 :1px :1px :black]
                               [:inset 0 0 :1px :red]})
  => (some-checker "0 1px 1px black, inset 0 0 1px red"
                   "inset 0 0 1px red, 0 1px 1px black"))


