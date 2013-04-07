(ns cljss.compilation.precompilation-test
  (:require [midje.sweet :as m])
  (:use cljss.data
        cljss.parse
        cljss.compilation.precompilation
        cljss.selectors))

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


(m/fact "The default decorator works as expected."
        (let [decorated (decorate-rule p-r default-decorator)
              s1 (:selector decorated)
              s2 (-> decorated :sub-rules first :selector)
              s3 (-> decorated :sub-rules second :selector)
              s4 (-> decorated :sub-rules second :sub-rules first :selector)
              
              depth1 (:depth decorated)
              depth2 (-> decorated :sub-rules first :depth)
              depth3 (-> decorated :sub-rules second :depth)
              depth4 (-> decorated :sub-rules second :sub-rules first :depth)]
          
          s1 => s1-expected
          s2 => s2-expected
          s3 => s3-expected
          s4 => s4-expected
          
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

(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))

(m/fact "flatten rule flattens a rule and its sub rules..."
        (flatten-rule p-r) 
        => (list p-r1 p-r2 p-r3 p-r4))
