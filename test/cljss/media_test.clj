(ns cljss.media-test
  (:use cljss.media
        cljss.rule
        cljss.protocols
        midje.sweet))

(fact "We can parse a media query"
  (parse (media "screen" 
                [:body :width "1000px"]
                [:h1   :font-size "1em"]))
  => {:exp "screen"
      :body [(parse-rule [:body :width "1000px"])
             (parse-rule [:h1   :font-size "1em"])]})
