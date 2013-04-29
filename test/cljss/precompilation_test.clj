(ns cljss.precompilation-test
  (:use cljss.precompilation
        cljss.protocols
        cljss.selectors.combination
        cljss.selectors.pseudos
        cljss.selectors.parent
        [midje.sweet :only (fact facts future-fact)]
        [cljss.parse :only (parse-rule)]
        [cljss.selectors.combination :only (combine)]
        [cljss.selectors.combinators :only (c-g+)]
        [cljss.compilation :only (depth-decorator)]))

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


(fact "The depth decorator associate a depth to its rule."
  (let [visited (visit p-r depth-decorator)
        depth1 (:depth visited)
        depth2 (-> visited :sub-rules first :depth)
        depth3 (-> visited :sub-rules second :depth)
        depth4 (-> visited :sub-rules second :sub-rules first :depth)]
    
    depth1 => 0
    depth2 => 1
    depth3 => 1
    depth4 => 2))


(def default-visitor
  (chain-visitors assoc-depth 
                  combine-or-replace-parent
                  simplify-selector))


(fact "The default decorator works as expected."
  (let [visited (visit p-r default-visitor)
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)
        s3 (-> visited :sub-rules second :selector)
        s4 (-> visited :sub-rules second :sub-rules first :selector)
              
        depth1 (:depth visited)
        depth2 (-> visited :sub-rules first :depth)
        depth3 (-> visited :sub-rules second :depth)
        depth4 (-> visited :sub-rules second :sub-rules first :depth)]
          
    s1 => s1-expected
    s2 => s2-expected
    s3 => s3-expected
    s4 => s4-expected
          
    depth1 => 0
    depth2 => 1
    depth3 => 1
    depth4 => 2))

(future-fact "use the simplify selector")


(fact "The chain-visitor fn behave such as"
  (visit p-r (chain-visitors assoc-depth
                             combine-or-replace-parent
                             simplify-selector))
  => (-> p-r
         (visit assoc-depth)
         (visit combine-or-replace-parent)
         (visit simplify-selector)))

(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))


(fact "flatten rule flattens a rule and its sub rules..."
  (flatten-rule p-r) 
  => (list p-r1 p-r2 p-r3 p-r4))


(fact "The combine selectors visitor recursively combines
  the selector of a rule to its sub rules."
  (let [visited (visit p-r combine-or-replace-parent)
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)
        s3 (-> visited :sub-rules second :selector)
        s4 (-> visited :sub-rules second :sub-rules first :selector)]
    s1 => s1-expected
    s2 => s2-expected
    s3 => s3-expected
    s4 => s4-expected))



(facts "We can replace the parent selector in nested rules"
  
  (fact "Parent selector can be used in pseudo classes"
    (let [a-rule [#{:section :div} 
                  :color :blue
                  [(-> & hover) :color :white]]
               
          visited (-> a-rule 
                        parse-rule 
                        (visit combine-or-replace-parent))
          s2 (-> visited :sub-rules first :selector)]
      
      s2 => #{(-> :section hover) 
              (-> :div hover)}))
         
  (fact "The parent selector can be used inside a set"
    (let [r [:section :color :blue
             [#{& :div} :colore :white]]
          visited (-> r parse-rule (visit combine-or-replace-parent))
          s2 (-> visited :sub-rules first :selector)]
      
      s2 => #{:section :div}))
         
  (fact "The parent selector can be used inside combined selectors"
    (let [r [:section :color :blue
             [[:a #{& :div}] :colore :white]]
          visited (-> r parse-rule (visit  combine-or-replace-parent))
          s2 (-> visited :sub-rules first :selector)]
      s2 => [:a #{:section :div}])))




(fact "The siplify decorator simplifies selector in a rule and its nested rules"
  (let [r [#{[[:div :p][:a []]] [#{:div :p []}[:a]]}
           :color :black
           [(c-g+ [:div :p][:a]) :color :blue]]
        visited (-> r parse-rule (visit  simplify-selector))
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)]
    s1 => (simplify #{[[:div :p][:a []]] [#{:div :p []}[:a]]})
    s2 => (simplify (c-g+ [:div :p][:a]))))
