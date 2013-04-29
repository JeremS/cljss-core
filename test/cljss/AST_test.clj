(ns cljss.AST-test
  (:require [cljss.compilation.styles :as styles])
  (:use cljss.AST
        midje.sweet
        cljss.protocols
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation :only (visit chain-visitors precompile-rule)])


(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))




(fact "We can compile properties"
  (compile-property [:color :blue]) => "color: blue;"
  (compile-property [:border ["1px" :solid :black]]) => "border: 1px solid black;")


(fact "We can compile a property map"
  (compile-property-map {:color :blue
                         :border ["1px" :solid :black]}
                        styles/compressed) 
  => (some-checker "color: blue;border: 1px solid black;"
                   "border: 1px solid black;color: blue;"))

(comment
(def default-decorator
  (chain-decorators depth-decorator 
                    combine-or-replace-parent-decorator
                    simplify-selectors-decorator))

(def r (-> [:div :color :blue
                 :border ["1px" :solid :black]]
           (parse-rule)
           (precompile-rule default-decorator)
           first))
)
(future-fact "Test that media queries compile well")

(future-fact "We can compile a rule"
  (css-compile r styles/compressed)
  => (some-checker "div {color: blue;border: 1px solid black;}"
                   "div {border: 1px solid black;color: blue;}"))
