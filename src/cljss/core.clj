(ns cljss.core
  (:use [clojure.pprint :only (pprint)]))


(comment

(defn assoc-ident
  ([rules] (assoc-ident rules 0))
  ([rules ident]
   (map (fn [{:keys [sub-rules] :as rule}]
          (assoc rule 
            :ident ident
            :sub-rules (assoc-ident sub-rules (inc ident))))
        rules)))

(defmulti type->keyword type :default :default)

(defmethod type->keyword clojure.lang.PersistentArrayMap [_] :map)
(defmethod type->keyword clojure.lang.PersistentVector   [_] :vector)
(defmethod type->keyword clojure.lang.PersistentHashSet  [_] :set)
(defmethod type->keyword clojure.lang.Keyword            [_] :keyword)
(defmethod type->keyword String                          [_] :string)
(defmethod type->keyword :default                        [_] :default)


(defmulti combine (fn [a b] [(type->keyword a)(type->keyword b)]))

(defmethod combine [:keyword :keyword] [k1 k2] (combine [k1] [k2]))
(defmethod combine [:keyword :vector ] [k v]   (combine [k]   v  ))
(defmethod combine [:keyword :set    ] [k s]   (combine [k]   s  ))
(defmethod combine [:vector  :keyword] [v k]   (combine  v   [k] ))
(defmethod combine [:set     :keyword] [s k]   (combine  s   [k] ))



(defmethod combine [:vector :vector] [v1 v2]
  (vec (concat v1 v2)))

(defmethod combine [:set :vector] [s v]
  (set (reduce #(conj %1 (combine %2 v)) #{} s)))

(defmethod combine [:vector :set] [v s]
  (set (reduce #(conj %1 (combine v %2)) #{} s)))

(defmethod combine [:set :set] [s1 s2]
  (set(for [e1 s1 e2 s2]
        (combine e1 e2))))


(defn flatten-rules [[{:keys [selection sub-rules] :as rule} & r]]
  (lazy-seq
   (when (seq rule)
     (let [sr (when (seq sub-rules)
                (map #(assoc % :selection (combine selection (:selection %))) sub-rules))]
       (cons (dissoc rule :sub-rules)
             (flatten-rules (if (seq sr) 
                              (concat sr r) 
                              r)))))))

(defmulti compile-selection type->keyword :default :default)

(defmethod compile-selection :default [s] (to-css s))
 
(defmethod compile-selection :vector [v]
  (->> v
       (map compile-selection)
       (interpose " ")
       (apply str)))

(defmethod compile-selection :set [s]
  (->> s
       (map compile-selection)
       (interpose ",\n")
       (partition 2 2 nil)
       (map (partial apply str))))


(compile-selection #{[:p :img :ol] [:section :img :ul] [:section :img :ol]
    [:p :img :ul]})

(compile-selection #{[:p :img :ol] [:section :img :ul] })

(defn compile-property [[prop val]]
  (let [prop (to-css prop)
        val (if (coll? val) 
              (->> val
                   (map to-css)
                   (interpose " ") 
                   (apply str)) 
              (to-css val))]
    (str prop ": " val ";")))


(defn compile-properties [m]
  (->> m 
       (map compile-property)
       (map (partial suffix \newline))
       (map (partial apply str))))


(compile-properties 
 {:border [:1px :solid :black]
  :color :blue})

(defn compile-inner-rule [{:keys [selection properties] :as rule}]
  (assoc rule :selection (compile-selection selection)
              :properties (compile-properties properties)))

(defn preffix [pre s]
  (if (seq pre) 
    (concat pre s)
    s))

(defn make-ident [i]
  (if (> i 0)
    (apply str (repeat i "    "))
    ""))

(defn preffix-identation [{:keys [selection properties ident]
                           :as rule}]
    (let [sel-ident (make-ident ident)
          prop-ident (make-ident (inc ident))]
      (assoc rule
        :selection (interleave (repeat sel-ident) selection)
        :properties (interleave (repeat prop-ident) properties))))

(defn compile-rule [{:keys [selection properties ident]
                     :as rule}]
  (let [rule (-> rule compile-inner-rule preffix-identation)]
    (str (apply str (:selection rule)) " {\n"
         (apply str (:properties rule))
         (make-ident ident) "}\n")))

(->> [[:a :color :blue {:border [:1px :solid :black]}]
      [#{:p :section} :color :blue {:border [:1px :solid :black]}
       [:img :border :red
        [#{:ul :ol} :style :none
         {:border [:1px :solid :black]
          :color :blue}]]]]
     (parse)
     (assoc-ident)
	 (flatten-rules)
     (map compile-inner-rule)
     (map preffix-identation)
     (pprint))

(->> [[:a :color :blue {:border [:1px :solid :black]}]
      [#{:p :section} :color :blue {:border [:1px :solid :black]}
       [:img :border :red
        [#{:ul :ol} :style :none
         {:border [:1px :solid :black]
          :color :blue}]]]]
     (parse)
     (assoc-ident)
	 (flatten-rules)
     (map compile-rule)
     (apply concat)
     (apply str)
     (print)
     )
  
)