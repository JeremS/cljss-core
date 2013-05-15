(ns cljss.precompilation-test
  (:use cljss.precompilation
        cljss.precompilation.visitor
        cljss.selectors.protocols
        [cljss.AST :only (media)]
        [cljss.parse :only (parse-rule)]
        [cljss.selectors :only (combine & hover)]
        [midje.sweet :only (fact facts future-fact)]))

(def r1 [:div :bgcolor :blue])
(def r2 (media "screen" :toto :toto
               [:a :color :blue]))
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))

(def s1-expected :div)
(def s2-expected "screen")
(def s3-expected (combine :div :a))
(def s4-expected (combine :div :p))
(def s5-expected (combine s4-expected :strong))


(fact "The depth decorator associate a depth to its rule."
  (let [visited (visit p-r assoc-depth)
        depth1 (:depth visited)
        depth2 (-> visited :sub-rules first :depth)
        depth3 (-> visited :sub-rules first :sub-rules first :depth)
        depth4 (-> visited :sub-rules second :depth)
        depth5 (-> visited :sub-rules second :sub-rules first :depth)]

    depth1 => 0
    depth2 => 1
    depth3 => 2
    depth4 => 1
    depth5 => 2))


(fact "The combine selectors visitor recursively combines
  the selector of a rule to its sub rules."
  (let [visited (visit p-r combine-or-replace-parent)
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)
        s3 (-> visited :sub-rules first :sub-rules first :selector)
        s4 (-> visited :sub-rules second :selector)
        s5 (-> visited :sub-rules second :sub-rules first :selector)]
    s1 => s1-expected
    s2 => s2-expected
    s3 => s3-expected
    s4 => s4-expected
    s5 => s5-expected))

(fact "The simplify decorator simplifies selector in a rule and its nested rules"
  (let [r [#{[[:div :p][:a []]] [#{:div :p []}[:a]]}
           :color :black
           [[[:div :p] :+ [:a]]
            :color :blue]]
        visited (-> r parse-rule (visit  simplify-selector))
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)]
    s1 => (simplify #{[[:div :p][:a []]] [#{:div :p []}[:a]]})
    s2 => (simplify [[:div :p] :+ [:a]])))


(fact "The default visitor works as expected."
  (let [visited (visit p-r default-visitor)
        s1 (:selector visited)
        s2 (-> visited :sub-rules first :selector)
        s3 (-> visited :sub-rules first :sub-rules first :selector)
        s4 (-> visited :sub-rules second :selector)
        s5 (-> visited :sub-rules second :sub-rules first :selector)
        s6 (-> visited :sub-rules first :sub-rules second :selector)


        depth1 (:depth visited)
        depth2 (-> visited :sub-rules first :depth)
        depth3 (-> visited :sub-rules first :sub-rules first :depth)
        depth4 (-> visited :sub-rules second :depth)
        depth5 (-> visited :sub-rules second :sub-rules first :depth)
        depth6 (-> visited :sub-rules first :sub-rules second :depth)]

    s1 => s1-expected
    s2 => s2-expected
    s3 => s3-expected
    s4 => s4-expected
    s5 => s5-expected
    s6 => :div

    depth1 => 0
    depth2 => 1
    depth3 => 2
    depth4 => 1
    depth5 => 2
    depth6 => 2))

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
             [[:a #{& :div}] :color :white]]
          visited (-> r parse-rule (visit  combine-or-replace-parent))
          s2 (-> visited :sub-rules first :selector)]
      s2 => [:a #{:section :div}])))



(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))

(fact "flatten-AST flattens a rule and its sub rules"
  (flatten-AST p-r)
  => (list p-r1 p-r2 p-r3 p-r4))
