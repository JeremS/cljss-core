(ns cljss.compilation-test
  (:require [midje.repl :as m])
  (:use cljss.compilation
        cljss.compilation.protocols
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation :only (precompile-rule)]))



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
           (precompile-rule)
           first))



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









                                           