;; ## Examples used in the README.

(ns cljss.examples
  (:refer-clojure :exclude [rem])
  (:use cljss.core))

(comment
  "to run an example"
  (println (apply cljss.core/css exX)))

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

   [[:div :> :a] ...]

   [[:div :+ :a] ...]

   [[:div "~" :a] ...]

   [[:section :div :> :p :+ :a :span] ...]

   [[:.class1 #{:ul :ol} :> :li] ...]
  ])


(def ex7
  [[(hover :a) ... ]
   [(first-letter :p) ... ]
   [(-> [:ul :> :li] hover (nth-child "even")) ... ]
   [(-> :a (att-sel "href=\"http://...\"")) ...]])



(def ex8
  [[:section ...
     [:div ...]]

   [:a ...
     [(-> & hover) ...]]

   [:section ...
     [#{& :div} ...]]])


(def ex9
  [[#{:div :section}
         :background-color :blue
         :width "800px"
         [:p :font-size "12pt"
          (media "(max-width: 500px)"
                 :font-size "5pt"
                 [:a :color :green])]
        (media "(max-width: 400px)"
               :width "400px")]])

(def ex10
  [[:a :a :a "inline1"]
   "inline2"])
