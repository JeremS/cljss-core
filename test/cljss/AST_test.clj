(ns ^{:author "Jeremy Schoffen."}
  cljss.AST-test
  (:use cljss.AST
        midje.sweet
        cljss.protocols
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation :only (precompile-rule)]
        [cljss.compilation :only (compile-rules styles)]))


(fact "Compiling inline css give the css"
  (css-compile (inline-css "a:hover { color: green; }") {})
  => "a:hover { color: green; }")


(fact "We can compile properties"
  (compile-property [:color :blue]) => "color: blue;"
  (compile-property [:border ["1px" :solid :black]]) => "border: 1px solid black;")


(fact "We can compile a property map"
  (compile-property-map {:color :blue
                         :border ["1px" :solid :black]}
                        (styles :compressed))
  => (some-checker "color: blue;border: 1px solid black;"
                   "border: 1px solid black;color: blue;"))



(def r1 [:div {:color :blue
               :border ["1px" :solid :black]}])

(def r2 (media "screen"
               [:body :width "1024px"]))

(def r3 [#{:div :section}
         :background-color :blue
         :width "800px"
         [:p :font-size "12pt"
          (media "(max-width: 500px)"
                 :font-size "5pt"
                 [:a :color :green])]
        (media "(max-width: 400px)"
               :width "400px")])

(facts "We can compile an ast"
  (fact "We can compile a simple rule"
    (->> r1 parse-rule precompile-rule (compile-rules (:compressed styles)))
    => (some-checker "div {color: blue;border: 1px solid black;}"
                   "div {border: 1px solid black;color: blue;}"))

  (fact "We can compile a media query"
    (->> r2 parse-rule precompile-rule (compile-rules (:compressed styles)))
    => #"\@media screen \{body \{width: 1024px;\}\}")

  ; this test might fail because the order of the properties, or the "set" like selector might change
  ; because of the use of maps and sets under the covers.
  (fact "We can compile a mix of everything"
    (->> r3 parse-rule precompile-rule (compile-rules (:compressed styles)))
    => (some-checker "div, section {width: 800px;background-color: blue;}section p, div p {font-size: 12pt;}@media (max-width: 500px) {div p a, section p a {color: green;}section p, div p {font-size: 5pt;}}@media (max-width: 400px) {div, section {width: 400px;}}")))
