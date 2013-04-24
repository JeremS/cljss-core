(ns cljss.examples
  (:refer-clojure :exclude [rem])
  (:use cljss.core))

(comment 
  "to run an example"
  (apply cljss.core/css exX))


(def ex1
  [[:section :color :black]
   [:div :color :white]])


(def ex2
  [[:#container 
      :background-color :black
      (list :width "900px" :height "400px")
      :border ["1px" :solid :white]
      {:position :relative
       :top "30px"
       :left "30px"}
      :color :blue]])



(defn css-float [side]
  {:float side})

(def default-box
  '(:padding ["0px" "20px"]
    :margin-left "10px"))

(def ex3
  [[:#nav (css-float :left) default-box]])



(def ex4
  [[:#container 
      :border "1px solid black"
      :padding-left "30px"
  
      [:a :color :green]
  
      [:section 
        :font-size "1em"
       
        ["p::first-letter"
          :font-size "2em"]]]])

(def ... '(:... :...))

(def ex5 
  [[:div.class1.class2  ...]
   ["div.class1.class2" ...]])


(def ex6
  [
   [[:div :a] ...] 
   
   [(c-> :div :a) ...]
   
   [(c-+ :div :a) ...]

   [(c-g+ :div :a) ...]

   [[:section (c-> :div (c-+ :p :a)) :span] ...] 

   [[:.class1 (c-> #{:ul :ol} :li)] ...] 
  ])



(def ex7
  [[(hover :a) ... ] 
   [(first-letter :p) ... ] 
   [(-> (c-> :ul :li) hover (nth-child "even")) ... ]])


(def ex8
  [[:section ...
     [:div ...]]
   
   [:a ...
     [(-> & hover) ...]]
   
   [:section ...
     [#{& :div} ...]]])
