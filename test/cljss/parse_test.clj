(ns cljss.parse-test
  (:use cljss.parse
        cljss.AST
        midje.sweet))

(facts "About parse-rule"
  (fact "It considers strings as inline css then returns an inline AST node"
    (parse-rule "a {color: blue;}") => (inline-css "a {color: blue;}"))

  (fact "It considers a character as inline css then returns an inline AST node"
    (parse-rule \newline) => (inline-css "\n"))

  (fact "It returns directly inline nodes"
    (parse-rule (inline-css "inline")) => (inline-css "inline")
    (parse-rule (css-comment "comment")) => (css-comment "comment"))

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

    (facts "It allows for sub rules"
      (fact "directly in the vector"
        (parse-rule [:div :border ["1px" :solid :black]
                     [:a :display :block]])
        => (contains {:sub-rules [(rule :a {:display :block} [])]}))

      (fact "or in a list thats in the vector"
        (parse-rule [:div (list :border ["1px" :solid :black]
                                [:a :display :block])])
        => (contains {:properties {:border ["1px" :solid :black]}
                      :sub-rules [(rule :a {:display :block} [])]}))))



  (facts "it construct Queries from media queries : "
    (fact "it makes a rule from a vector"
      (parse-rule (media "screen" :color :blue))
      => (contains {:selector "screen"
                    :properties {:color :blue}
                    :sub-rules []}))

    (fact "it accepts (prop-name val) as properties declaration"
      (parse-rule (media "screen" :color :blue :width "10px"))
      => (contains {:properties {:color :blue :width "10px"}}))


    (fact "it accepts map as property declacation"
      (parse-rule (media "print" {:color :blue
                                  :width "10px"}))
      => (contains {:properties {:color :blue
                                              :width "10px"}}))

    (fact "it accepts a lists as property declacation"
      (parse-rule (media "screen" (list :color :blue
                                        :width "10px")))
      => (contains {:properties {:color :blue
                                   :width "10px"}}))

    (fact "it accepts a mix of properties declaration style"
      (parse-rule (media "screen"
                         :border ["1px" :solid :black]
                         {:color :blue
                          :width "10px"}))
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue
                                 :width "10px"}})

      (parse-rule (media "screen"
                         {:border ["1px" :solid :black]}
                         :color :blue
                         :width "10px"))
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue
                                 :width "10px"}})

      (parse-rule (media "screen"
                         :border ["1px" :solid :black]
                         {:color :blue}
                         '(:width "10px")))
      => (contains {:properties {:border ["1px" :solid :black]
                                 :color :blue
                                 :width "10px"}}))

    (fact "It allows for sub rules"
      (parse-rule (media "screen"
                         [:a :display :block]))
      => (contains {:sub-rules [(rule :a {:display :block} [])]}))))

(fact "We can parse a bunch of rules"
  (parse-rules [[:a :a :a]
                (list [:b :b :b]
                      [:c :c :c])
                [:d :d :d]])
  => [(rule :a {:a :a})
      (rule :b {:b :b})
      (rule :c {:c :c})
      (rule :d {:d :d})])

