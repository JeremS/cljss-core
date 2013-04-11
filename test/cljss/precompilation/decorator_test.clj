(ns cljss.precompilation.decorator-test
  (:require [midje.sweet :as m])
  (:use cljss.precompilation.decorator
        [cljss parse selectors]))


(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))


(m/fact "The depth decorator associate a depth to its rule."
        (let [decorated (decorate-rule p-r depth-decorator)
              depth1 (:depth decorated)
              depth2 (-> decorated :sub-rules first :depth)
              depth3 (-> decorated :sub-rules second :depth)
              depth4 (-> decorated :sub-rules second :sub-rules first :depth)]

          depth1 => 0
          depth2 => 1
          depth3 => 1
          depth4 => 2))


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


(m/fact "The assoc-parent-selector-decorator recursively associate
        the selector of a rule to its sub rules as their parent selector."
        (let [decorated (decorate-rule p-r assoc-parent-selector-decorator)
              ps1 (:parent-sel decorated)
              ps2 (-> decorated :sub-rules first :parent-sel)
              ps3 (-> decorated :sub-rules second :parent-sel)
              ps4 (-> decorated :sub-rules second :sub-rules first :parent-sel)]
          ps1 => []
          ps2 => (first r1)
          ps3 => (first r1)
          ps4 => (first r3)))



(m/fact "The default decorator works as expected."
        (let [decorated (decorate-rule p-r default-decorator)
              s1 (:selector decorated)
              s2 (-> decorated :sub-rules first :selector)
              s3 (-> decorated :sub-rules second :selector)
              s4 (-> decorated :sub-rules second :sub-rules first :selector)
              
              ps1 (:parent-sel decorated)
              ps2 (-> decorated :sub-rules first :parent-sel)
              ps3 (-> decorated :sub-rules second :parent-sel)
              ps4 (-> decorated :sub-rules second :sub-rules first :parent-sel)
              
              depth1 (:depth decorated)
              depth2 (-> decorated :sub-rules first :depth)
              depth3 (-> decorated :sub-rules second :depth)
              depth4 (-> decorated :sub-rules second :sub-rules first :depth)]
          
          s1 => s1-expected
          s2 => s2-expected
          s3 => s3-expected
          s4 => s4-expected
          
          ps1 => []
          ps2 => s1-expected
          ps3 => s1-expected
          ps4 => s3-expected
          
          depth1 => 0
          depth2 => 1
          depth3 => 1
          depth4 => 2))


(def check-decorator 
  "this decorator just tags each rule with :check true"
  (decorator
   (fn [r env]
     (list (assoc r :check true)
           env))))

(m/fact "The chain-decorator fn behave such as"
        (decorate-rule p-r (chain-decorators check-decorator 
                                             combine-selector-decorator
                                             depth-decorator))
        => (-> p-r
               (decorate-rule check-decorator)
               (decorate-rule combine-selector-decorator)
               (decorate-rule depth-decorator)))
