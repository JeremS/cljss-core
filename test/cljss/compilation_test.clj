(ns cljss.compilation-test
  (:require [midje.repl :as m])
  (:use cljss.data
        cljss.parse
        cljss.compilation
        cljss.compilation.precompilation
        [clojure.pprint :only (pprint)]))



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









                                           