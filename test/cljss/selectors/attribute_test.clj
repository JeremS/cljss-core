(ns ^{:author "Jeremy Schoffen."}
  cljss.selectors.attribute-test
  (:require [clojure.string :as string])
  (:use cljss.selectors.attribute
        cljss.protocols
        cljss.selectors.protocols
        cljss.selectors.core
        [cljss.selectors.parent :only (&)]
        midje.sweet))


(fact "We can add pseudo attribute selectors to simple selectors"
  (-> "div" (att-sel "title") compile-as-selector) => "div[title]"
  (-> "div" (att-sel "title") (compile-as-selector {})) => "div[title]"
  (-> :div (att-sel "title" "alt") compile-as-selector)  => "div[title][alt]"
  (-> :div (att-sel "title" "alt") (compile-as-selector {}))  => "div[title][alt]")


(fact "We can add more than one attribute selector"
  (-> :div (att-sel "title|=somthing") (att-sel "alt~=other") compile-as-selector)
  => "div[title|=somthing][alt~=other]"

  (-> :div (att-sel "title|=somthing" "alt~=other") compile-as-selector)
  => "div[title|=somthing][alt~=other]")


(fact "We can use attribute selectors on combined selectors"
  (-> [:#id :a] (att-sel "title^=somthing") compile-as-selector)
  => "#id a[title^=somthing]"

  (-> [:#id :> :p :> :a] (att-sel "title*=somthing") compile-as-selector)
  => "#id > p > a[title*=somthing]"

  (-> #{:#id :a} (att-sel "href$=\"http://...\"") compile-as-selector)
  => (some-checker "#id[href$=\"http://...\"], a[href$=\"http://...\"]"
                   "a[href$=\"http://...\"], #id[href$=\"http://...\"]"))

(fact "Simplify selector with attribute selection gives selection of the simplifiction"
  (-> [:section #{:div :p} :span] (att-sel "title") simplify)
  => (-> [:section #{:div :p} :span] simplify (att-sel "title")))


(fact "We can test for parent use inside attribute selectors"
  (-> [:section #{:div :p} :span] (att-sel "title") parent?) => falsey
  (-> [:section #{:div &} :span] (att-sel "title") parent?) => truthy)


(fact "We can replace the parent selector"
  (-> [:section #{:div &} :span] (att-sel "title") (replace-parent :#parent))
  => (-> [:section #{:div :#parent} :span] (att-sel"title")))
