(ns cljss.selectors.combination-test
  (:require [midje.sweet :as m])
  (:use cljss.selectors.combination
        cljss.selectors.basic
        cljss.selectors.combinators))


(m/facts "About selector combination"
         (m/fact "string and keyword selectors can be combined
                 and give a path like selector"
                 (combine :div :a)   => [:div :a]
                 (combine "div" "a") => ["div" "a"]
                 (combine :div "a")  => [:div "a"]
                 (combine "div" :a)  => ["div" :a])
         
         (m/fact "combining basic selectors and combined selectors with a combined one give a vector of the two"
                 (combine :div  [:p :a]) => [:div [:p :a]]
                 (combine "div" [:p :a]) => ["div" [:p :a]]
                 
                 (combine [:div :p] :a)  => [[:div :p] :a]
                 (combine ["div" :p] :a) => [["div" :p] :a]
                 
                 (combine ["div" :p] [:a :span]) => [["div" :p] [:a :span]])
         
         (m/fact "combining one of the previous kind of selectors
                 with a set selector gives the set of the combinations"
                 (combine #{:div :section} :p) => #{(combine :div :p) 
                                                    (combine :section :p)}
                 
                 
                 (combine :p #{:div :section}) => #{(combine :p :div)
                                                    (combine :p :section)}
                 
                 
                 (combine #{:div :section} [:p :a]) => #{(combine :div [:p :a])
                                                         (combine :section [:p :a])}
                 
                 (combine [:p :a] #{:div :section}) => #{(combine [:p :a] :div)
                                                         (combine [:p :a] :section)}
                 
                 (combine #{:div :section} #{:p :a}) => #{[:div :p] 
                                                          [:div :a]
                                                          [:section :p]
                                                          [:section :a]}))