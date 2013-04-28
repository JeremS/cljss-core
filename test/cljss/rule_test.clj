(ns cljss.rule-test
  (:require [cljss.compilation :refer (depth-decorator depth)]
            [cljss.compilation.styles :as styles])
  (:use cljss.rule
        midje.sweet
        cljss.protocols
        [cljss.precompilation :only (decorate-rule chain-decorators precompile-rule)]
        [cljss.selectors :only (combine-or-replace-parent-decorator simplify-selectors-decorator)]))

(facts "About parse-rule"
  (facts "it construct rules from vectors : "
    (fact "it makes a rule from a vector"
      (parse-rule [:a :color :blue])
      => (rule :a
               {:color :blue}
               []))
                  
    (fact "it accepts (prop-name val) as properties declaration"
      (parse-rule [:a :color :blue :width "10px"])
      => (contains {:properties {:color :blue :width "10px"}}))


    (fact "it accepts map as property declacation"
      (parse-rule [:a {:color :blue 
                       :width "10px"}])
      => (contains {:properties {:color :blue 
                                              :width "10px"}}))
    
    (fact "it accepts a lists as property declacation"
      (parse-rule [:a (list :color :blue 
                            :width "10px")])
      => (contains {:properties {:color :blue 
                                   :width "10px"}}))

    (fact "it accepts a mix of properties declaration style"
      (parse-rule [:a 
                   :border ["1px" :solid :black]
                   {:color :blue 
                    :width "10px"}])
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue 
                                 :width "10px"}})

      (parse-rule [:a       
                   {:border ["1px" :solid :black]}
                   :color :blue 
                   :width "10px"])
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue 
                                 :width "10px"}})
      
      (parse-rule [:a 
                   :border ["1px" :solid :black]
                   {:color :blue} 
                   '(:width "10px")])           
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue 
                                 :width "10px"}}))
         
    (fact "It allows for sub rules"
      (parse-rule [:div :border ["1px" :solid :black]
                    [:a :display :block]])
      => (contains {:sub-rules [(rule :a {:display :block} [])]}))))



(def r1 [:div :bgcolor :blue])
(def r2 [:a :color :white])
(def r3 [:p :color :green])
(def r4 [:strong :color :black])


(def r (conj r1 r2 (conj r3 r4)))
(def p-r (parse-rule r))


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
  (css-compile r styles/compressed)
  => (some-checker "div {color: blue;border: 1px solid black;}"
                   "div {border: 1px solid black;color: blue;}"))
