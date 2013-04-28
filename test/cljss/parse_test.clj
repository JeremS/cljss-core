(ns cljss.parse-test
  (:use cljss.parse
        midje.sweet
        cljss.rule))

(fact "parse take a seq of vectors representing rules and return a vector of rules"
  (parse [[:a :color :blue]
          [:div :back-ground-color :green]])
  => [(rule :a {:color :blue} [])
      (rule :div {:back-ground-color :green} [])])