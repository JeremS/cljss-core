(ns cljss.selectors-test
  (:refer-clojure :exclude [empty not])
  (:require [midje.repl :as m])
  (:use cljss.selectors
        cljss.selectors.pseudos
        cljss.selectors.parent
        cljss.selectors.protocols
        cljss.compilation.protocols
        [cljss.parse :only (parse-rule)]
        [cljss.precompilation :only (decorate-rule)]
        [cljss.selectors.combination :only (combine)]
        [cljss.selectors.combinators :only (c-g+)]))

(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))


(def s1-expected :div)
(def s2-expected (combine :div :a))
(def s3-expected (combine :div :p))
(def s4-expected (combine s3-expected :strong))


(m/fact "The combine selectors decorator recursively combines
        the selector of a rule to its sub rules."
        (let [decorated (decorate-rule p-r combine-selector-decorator)
              s1 (:selector decorated)
              s2 (-> decorated :sub-rules first :selector)
              s3 (-> decorated :sub-rules second :selector)
              s4 (-> decorated :sub-rules second :sub-rules first :selector)]
          s1 => s1-expected
          s2 => s2-expected
          s3 => s3-expected
          s4 => s4-expected))


(m/facts "We can replace the parent selector in nested rules"
         
         
         (m/fact "Parent selector can be used in pseudo classes"
                 (let [a-rule [#{:section :div} 
                       :color :blue
                        [(-> & hover) :color :white]]
               
                       decorated (-> a-rule 
                                     parse-rule 
                                     (decorate-rule  combine-selector-decorator))
                       s2 (-> decorated :sub-rules first :selector)]
                   s2 => #{(-> :section hover) 
                           (-> :div hover)}))
         
         (m/fact "The parent selector can be used inside a set"
                 (let [r [:section :color :blue
                          [#{& :div} :colore :white]]
                       decorated (-> r parse-rule (decorate-rule  combine-selector-decorator))
                       s2 (-> decorated :sub-rules first :selector)]
                   s2 => #{:section :div}))
         
         (m/fact "The parent selector can be used inside combined selectors"
                 (let [r [:section :color :blue
                          [[:a #{& :div}] :colore :white]]
                       decorated (-> r parse-rule (decorate-rule  combine-selector-decorator))
                       s2 (-> decorated :sub-rules first :selector)]
                   s2 => [:a #{:section :div}])))




(m/fact "The siplify decorator simplifies selector in a rule and its nested rules"
        (let [r [#{[[:div :p][:a []]] [#{:div :p []}[:a]]}
                 :color :black
                 [(c-g+ [:div :p][:a]) :color :blue]]
              decorated (-> r parse-rule (decorate-rule  simplify-selectors-decorator))
              s1 (:selector decorated)
              s2 (-> decorated :sub-rules first :selector)]
          s1 => (simplify #{[[:div :p][:a []]] [#{:div :p []}[:a]]})
          s2 => (simplify (c-g+ [:div :p][:a]))))