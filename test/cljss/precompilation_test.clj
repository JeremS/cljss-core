(ns cljss.precompilation-test
  (:use cljss.precompilation
        cljss.parse
        cljss.selectors.combination
        midje.sweet
        [cljss.selectors :only (combine-or-replace-parent-decorator)]
        [cljss.compilation :only (depth-decorator)]))

(def depth (keyword "cljss.compilation" "depth"))
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


(def assoc-parent-selector-decorator
  (decorator []
    (fn [r parent-sel]
      (list (assoc r :parent-sel parent-sel)
            (:selector r)))))

(fact "The assoc-parent-selector-decorator recursively associate
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


(def default-decorator
  (chain-decorators combine-or-replace-parent-decorator 
                    depth-decorator
                    assoc-parent-selector-decorator))


(fact "The default decorator works as expected."
  (let [decorated (decorate-rule p-r default-decorator)
        s1 (:selector decorated)
        s2 (-> decorated :sub-rules first :selector)
        s3 (-> decorated :sub-rules second :selector)
        s4 (-> decorated :sub-rules second :sub-rules first :selector)
              
        ps1 (:parent-sel decorated)
        ps2 (-> decorated :sub-rules first :parent-sel)
        ps3 (-> decorated :sub-rules second :parent-sel)
        ps4 (-> decorated :sub-rules second :sub-rules first :parent-sel)
              
        depth1 (depth decorated)
        depth2 (-> decorated :sub-rules first depth)
        depth3 (-> decorated :sub-rules second depth)
        depth4 (-> decorated :sub-rules second :sub-rules first depth)]
          
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

(fact "The chain-decorator fn behave such as"
  (decorate-rule p-r (chain-decorators check-decorator 
                                       combine-or-replace-parent-decorator
                                       depth-decorator))
  => (-> p-r
         (decorate-rule check-decorator)
         (decorate-rule combine-or-replace-parent-decorator)
         (decorate-rule depth-decorator)))

(def p-r1 (parse-rule r1))
(def p-r2 (parse-rule r2))
(def p-r3 (parse-rule r3))
(def p-r4 (parse-rule r4))

(fact "flatten rule flattens a rule and its sub rules..."
  (flatten-rule p-r) 
  => (list p-r1 p-r2 p-r3 p-r4))
