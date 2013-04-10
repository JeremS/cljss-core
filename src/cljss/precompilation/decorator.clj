(ns cljss.precompilation.decorator)

(defrecord Decorator [f env])


(comment
(defn decorator [env f]
  (let [id (java.util.UUID/randomUUID)
        env {id env}]
    (Decorator.
     (fn [v aenv] 
       (f v (get id env)))
     env)))
)


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