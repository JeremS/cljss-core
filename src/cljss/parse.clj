(ns cljss.parse
  (:use [cljss.rule :only (rule parse-rule)]))




(defn parse [rules]
  (map parse-rule rules))
