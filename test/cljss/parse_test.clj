(ns cljss.parse-test
  (:use cljss.parse
        midje.sweet))

(facts "About make-rule"
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


(fact "parse take a seq of vectors representing rules and return a vector of rules"
  (parse [[:a :color :blue]
          [:div :back-ground-color :green]])
  => [(rule :a {:color :blue} [])
      (rule :div {:back-ground-color :green} [])])
