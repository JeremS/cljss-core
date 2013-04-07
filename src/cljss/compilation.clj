(ns cljss.compilation
  (:require [cljss.selectors :as sel]
            [clojure.string :as string])
  (:use cljss.data))


(defrecord Decorator [f env])

(defn decorator
  "Construct a decorator, is a function that 
  decorate, transforms a rule given a environment.
  the environment env, must be a map.
  
  When no environment is provided
  an empty map is used as the default one."
  ([f]
   (Decorator. f {}))
  ([f env]
   (Decorator. f env)))


(defn- chain-2-decorators [d1 d2]
  (let [{f1 :f env1 :env} d1
        {f2 :f env2 :env} d2]
    (decorator
     (fn [r env]
       (let [[r env] (f1 r env)]
         (f2 r env)))
     (merge env1 env2))))

(defn chain-decorators 
  "Allows to compose from left to right
  the behaviour of decorators.
  
  Be careful, the default environments of each
  decorators are merged, if they "
  [d1 d2 & ds]
  (reduce chain-2-decorators 
          (list* d1 d2 ds)))

(defn- dr [r f env]
  (let [[new-r new-env] (f r env)
        new-sub-rules (map #(dr % f new-env)
                          (:sub-rules r))]
    (assoc new-r 
      :sub-rules new-sub-rules)))

(defn decorate-rule 
  "Applies a decorator to a rule and recursively 
  to its sub rules."
  [r {:keys [f env]}]
  (dr r f env))

(def depth-decorator
  "Attach to a rule its depth, level in which
  it is embeded."
  (decorator 
   (fn [r {d :depth :as env}]
     (list (assoc r :depth d)
           (update-in env [:depth] inc)))
   {:depth 0}))

(def ancestors-decorator
  "Attach to a rule its chain of ancestors."
  (decorator
   (fn [r {ps :ancestors :as env}]
     (list (assoc r :ancestors ps)
           (update-in env [:ancestors] 
                      conj (assoc r :sub-rules []))))
   {:ancestors []}))


(def default-decorator
  (chain-decorators ancestors-decorator depth-decorator))


(defn- dr [r f env]
  (let [[new-r new-env] (f r env)
        new-sub-rules (map #(dr % f  new-env)
                          (:sub-rules r))]
    (assoc new-r 
      :sub-rules new-sub-rules)))

(defn decorate-rule 
  "Given a rule and a decorator, decorate the rule and its sub rules"
  [r {:keys [f env]}]
  (dr r f env))


(defn flatten-rule 
  "Given a rule returns a flatten list of the rule and its
  sub rules"
  [{:as r}]
  (let [new-r (assoc r :sub-rules '())
        sub-rs (:sub-rules r)]
    (cons new-r
          (mapcat flatten-rule sub-rs))))

(defn flatten-rules 
  "See flatten-rule"
  [rs]
  (mapcat #(flatten-rule % 0) rs))

(defn make-definitive-selector [r]
  (let [r-sel (:selector r)
        ancestors-sels (mapv :selector (:ancestors r))]
    (reduce sel/combine 
            (conj ancestors-sels r-sel))))

(defn definitive-selector 
  "Give a rule its final selector wich is a the combination
  of its ancestors and its own."
  [r]
  (-> r
      (assoc :selector (make-definitive-selector r))
      (dissoc :ancestors)))


(defn compile-seq-then-join [v compile-fn c]
  (->> v
       (map compile-fn)
       (string/join c)))


(defprotocol CssSelector
  (compile-as-selector [this]))

(defn compile-path-sel [sel]
  (compile-seq-then-join sel 
                         compile-as-selector
                         \space))

(defn compile-set-sel [sel]
  (compile-seq-then-join sel 
                         compile-as-selector
                         ", "))

(extend-protocol CssSelector
  String
  (compile-as-selector [this] this)
  
  clojure.lang.Keyword
  (compile-as-selector [this] (name this))
  
  clojure.lang.PersistentVector
  (compile-as-selector [this]
    (compile-path-sel this))
  
  clojure.lang.IPersistentSet
  (compile-as-selector [this]
    (compile-set-sel this)))


(-> #{} type supers)

(defprotocol CssPropertyName
  (compile-as-property-name [this]))


(extend-protocol CssPropertyName
  clojure.lang.Keyword
  (compile-as-property-name [this] (name this))
  
  String
  (compile-as-property-name [this] this))


(defprotocol CssPropertyValue
  (compile-as-property-value [this]))


(defn compile-seq-property-value [s]
  (compile-seq-then-join s
                         compile-as-property-value 
                         \space))


(extend-protocol CssPropertyValue
  String
  (compile-as-property-value [this] this)
  
  clojure.lang.Keyword
  (compile-as-property-value [this] (name this))
  
  clojure.lang.PersistentVector
  (compile-as-property-value [this]
    (compile-seq-property-value this))
  
  clojure.lang.PersistentList
  (compile-as-property-value [this]
    (compile-seq-property-value this)))



(def ^:dynamic *end-property-line* "")
(def ^:dynamic *start-properties* "")
(def ^:dynamic *end-properties* "")
(def ^:dynamic *general-indent* "")
(def ^:dynamic *indent* "")
(def ^:dynamic *property-indent* "")

(defn compile-property [[p-name p-val]]
  (let [s-name (compile-as-property-name p-name)
        s-val  (compile-as-property-value p-val)]
    (str s-name ": " s-val \; *end-property-line*)))



(defn- add-property-indent [props]
  (interleave (repeat (str *general-indent* *property-indent*))
               props))

(defn compile-property-map [m]
  (->> m
       (map compile-property )
       (add-property-indent )
       (string/join )))

(defn compile-rule [{:keys [selector properties depth]}]
  (binding [*general-indent* (apply str (repeat depth *indent*))]
    (let [compiled-selector   (compile-as-selector selector)
          compiled-properties (compile-property-map properties)]
      (str *general-indent* compiled-selector " {" *start-properties*
                                compiled-properties 
           *general-indent* "}"))))

