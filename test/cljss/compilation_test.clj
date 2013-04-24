(ns cljss.compilation-test
  (:require [cljss.compilation.styles :as styles])
  (:use cljss.compilation
        cljss.protocols
        midje.sweet
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation 
           :only (decorate-rule chain-decorators precompile-rule)]
        [cljss.selectors 
           :only (combine-or-replace-parent-decorator simplify-selectors-decorator)]))

(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))

(def depth (keyword "cljss.compilation" "depth"))

(fact "The depth decorator associate a depth to its rule."
  (let [decorated (decorate-rule p-r depth-decorator)
        depth1 (depth decorated)
        depth2 (-> decorated :sub-rules first depth)
        depth3 (-> decorated :sub-rules second depth)
        depth4 (-> decorated :sub-rules second :sub-rules first depth)]
    
    depth1 => 0
    depth2 => 1
    depth3 => 1
    depth4 => 2))

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
         
  (fact "it compiles vectors and lists to the space separated compilation of their values"
    (compile-as-property-value ["1px" :solid :black]) => "1px solid black"))

(fact "We can compile properties"
  (compile-property [:color :blue]) => "color: blue;"
  (compile-property [:border ["1px" :solid :black]]) => "border: 1px solid black;")


(fact "We can compile a property map"
  (compile-property-map {:color :blue
                         :border ["1px" :solid :black]}
                        styles/compressed) 
  => (some-checker "color: blue;border: 1px solid black;"
                   "border: 1px solid black;color: blue;"))


(def default-decorator
  (chain-decorators depth-decorator 
                    combine-or-replace-parent-decorator
                    simplify-selectors-decorator))

(def r (-> [:div :color :blue
                 :border ["1px" :solid :black]]
           (parse-rule)
           (precompile-rule default-decorator)
           first))



(fact "We can compile a rule"
  (compile-rule r styles/compressed)
  => (some-checker "div {color: blue;border: 1px solid black;}"
                   "div {border: 1px solid black;color: blue;}"))










                                           