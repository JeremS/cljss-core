(ns cljss.parse-test
  (:require [midje.repl :as m])
  (:use cljss.parse))

(m/facts "About make-rule"
         (m/facts "it construct rules from vectors : "
                  (m/fact "it makes a rule from a vector"
                          (parse-rule [:a :color :blue])
                           => (rule :a
                                      {:color :blue}
                                      []))
                  
                  (m/fact "it accepts (prop-name val) as properties declaration"
                          (parse-rule [:a :color :blue :width "10px"])
                          => (m/contains {:properties {:color :blue :width "10px"}}))
         
                  
                  (m/fact "it accepts map as property declacation"
                          (parse-rule [:a {:color :blue 
                                          :width "10px"}])
                          => (m/contains {:properties {:color :blue 
                                              :width "10px"}}))
                  (m/fact "it accepts a lists as property declacation"
                          (parse-rule [:a (list :color :blue 
                                               :width "10px")])
                          => (m/contains {:properties {:color :blue 
                                              :width "10px"}}))
                  
                  (m/fact "it accepts a mix of properties declaration style"
                          (parse-rule [:a 
                                      :border ["1px" :solid :black]
                                      {:color :blue 
                                       :width "10px"}])
                          => (m/contains {:properties {:border ["1px" :solid :black]
                                                       :color :blue 
                                                       :width "10px"}})
                          (parse-rule [:a 
                                      {:border ["1px" :solid :black]}
                                      :color :blue 
                                      :width "10px"])
                          => (m/contains {:properties {:border ["1px" :solid :black]
                                                       :color :blue 
                                                       :width "10px"}})
                          (parse-rule [:a 
                                      :border ["1px" :solid :black]
                                      {:color :blue} 
                                      '(:width "10px")])
                          => (m/contains {:properties {:border ["1px" :solid :black]
                                                       :color :blue 
                                                       :width "10px"}}))
         
                  (m/fact "It allows for sub rules"
                          (parse-rule [:div :border ["1px" :solid :black]
                                      [:a :display :block]])
                          => (m/contains {:sub-rules [(rule :a {:display :block} [])]}))))


(m/fact "parse take a seq of vectors representing rules and return a vector of rules"
        (parse [[:a :color :blue]
                [:div :back-ground-color :green]])
        => [(rule :a {:color :blue} [])
            (rule :div {:back-ground-color :green} [])])
