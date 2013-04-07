(ns cljss.compilation-test
  (:require [midje.repl :as m])
  (:use cljss.data
        cljss.parse
        cljss.compilation
        [clojure.pprint :only (pprint)]))

(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))


(m/fact "The parents decorator associate each rule with its 
        a sequence of its parents from top most to the immediate one."
        (decorate-rule p-r ancestors-decorator)
        => (assoc p-r1 :ancestors []
             :sub-rules [(assoc p-r2 :ancestors [p-r1])
                         (assoc p-r3 :ancestors [p-r1]
                           :sub-rules [(assoc p-r4 :ancestors [p-r1 p-r3])])]))

(def check-decorator 
  "this decorator just tags each rule with :check true"
  (decorator
   (fn [r env]
     (list (assoc r :check true)
           env))))

(m/fact "The chain-decorator chain decorators behavior such as"
        (decorate-rule p-r (chain-decorators check-decorator 
                                             ancestors-decorator 
                                             depth-decorator))
        => (-> p-r
               (decorate-rule check-decorator)
               (decorate-rule ancestors-decorator)
               (decorate-rule depth-decorator)))

(m/fact "flatten rule flattens a rule and its sub rules..."
        (flatten-rule p-r) 
        => (list p-r1 p-r2 p-r3 p-r4))



(def fp-r 
  "Here the rule has bean parsed then flattened."
  (-> p-r 
  (decorate-rule default-decorator)
  (flatten-rule )))

(def sfpr
  "Here we combine the selectors of each rule so that 
  each rule has its definitive selector."
  (map definitive-selector fp-r))


(m/fact "make-definitive-selector combines the selector of a rule
        with those of its parents"
        (map :selector sfpr) => [:div [:div :a] [:div :p] [:div :p :strong]])


(m/facts "About Compiling selectors"
         (m/fact "Compiling a String selector gives the same string"
                 (compile-as-selector "div") => "div"
                 (compile-as-selector "a") => "a"
                 (compile-as-selector ".class") => ".class")
         
         (m/fact "Compiling a Keyword selector gives the name of the keyword"
                 (compile-as-selector :div) => "div"
                 (compile-as-selector :a) => "a"
                 (compile-as-selector :.class) => ".class")
         
         (m/fact "Compiling a path like selector give the path of the compiled selectors"
                 (compile-as-selector [:div "p" :.class]) => "div p .class")
         
         (m/fact "Compiling a set of selectors give the set of the compiled selectors"
                 (compile-as-selector #{:div ["p" :.class]}) 
                 => (m/some-checker "div, p .class"
                                    "p .class, div")))


(m/facts "About compiling css property names"
         (m/fact "it compiles a string to the same string"
                 (compile-as-property-name "color" => "color"))
         
         (m/fact "it compiles a keyword to its name string"
                 (compile-as-property-name :color) => "color"))

(m/facts "About compiling css property values"
         (m/fact "it compiles a string to the same string"
                 (compile-as-property-value "black" => "black"))
         
         (m/fact "it compiles a keyword to its name string"
                 (compile-as-property-value :black) => "black")
         
         (m/fact "it compiles vectors and lists to the space separated compilation of their values"
                 (compile-as-property-value ["1px" :solid :black]) => "1px solid black"))

(m/fact "We can compile properties"
        (compile-property [:color :blue]) => "color: blue;"
        (compile-property [:border ["1px" :solid :black]]) => "border: 1px solid black;")


(m/fact "We can compile a property map"
        (compile-property-map {:color :blue
                               :border ["1px" :solid :black]}) 
        => (m/some-checker "color: blue;border: 1px solid black;"
                           "border: 1px solid black;color: blue;"))



(def r (-> [:div :color :blue
                 :border ["1px" :solid :black]]
           (parse-rule)
           (decorate-rule default-decorator)
           (flatten-rule)
           first
           definitive-selector))


(m/fact "We can compile a rule"
        (compile-rule r)
        => (m/some-checker "div {color: blue;border: 1px solid black;}"
                            "div {border: 1px solid black;color: blue;}"))


(comment
(binding [*end-property-line* "\n"
          *start-properties* "\n"
          *end-properties* ""
          *general-indent* ""
          *indent* ""
          *property-indent* "  "]
  (println (compile-rule (assoc r :depth 3))))

)









                                           