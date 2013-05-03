(ns ^{:author "Jeremy Schoffen."}
  cljss.AST
  (:require [clojure.string :as string])
  (:use cljss.protocols))


(declare compile-property-map)

(defn- compile-css [sel inner {start :start-properties
                                 sep :rules-separator
                               indent :outer-indent}]
  (str indent sel " {" start
              inner
       indent "}" sep))

(defn make-indent [n unit]
  (apply str (repeat n unit)))

(defrecord Rule [selector properties sub-rules]
  Tree
  (children [_] sub-rules)
  (assoc-children [this children]
    (assoc this :sub-rules children))

  CSS
  (css-compile [this {start :start-properties
                      unit :indent-unit
                      :as style}]
    (let [d (:depth this)
          outer (make-indent d unit)
          inner (make-indent (inc d) unit)
          new-style (assoc style :outer-indent outer :inner-indent inner)
          compiled-selector (compile-as-selector selector)
          compiled-properties (compile-property-map properties new-style)]

    (compile-css compiled-selector compiled-properties new-style))))



(defn rule
  ([selection ]
   (rule selection {}))
  ([selection properties]
   (rule selection properties []))
  ([selection properties sub-rules]
   (Rule. selection properties sub-rules)))


(defrecord Query [selector body properties sub-rules]
  Tree
  (children [_] sub-rules)
  (assoc-children [this children]
    (assoc this :sub-rules children))

  CSS
  (css-compile [this {start :start-properties
                       unit :indent-unit
                        sep :rules-separator
                      :as style}]
    (let [d (:depth this)
          outer (make-indent d unit)
          inner (make-indent (inc d) outer)
          new-style (assoc style :outer-indent outer :inner-indent inner)
          sel (str "@media " selector)
          compiled-sub-rules (->> sub-rules
                                 (map #(css-compile % new-style))
                                 (string/join sep ))]
      (compile-css sel (str compiled-sub-rules sep) new-style))))

(defn media [sel & body]
  (Query. sel (vec body) {} []))



(defn compile-property [[p-name p-val]]
  (let [s-name (compile-as-property-name p-name)
        s-val  (compile-as-property-value p-val)]
    (str s-name ": " s-val \;)))


(defn compile-property-map [m style]
  (let [{inner :inner-indent
           sep :property-separator} style]
    (->> m
         (map compile-property )
         (mapcat #(list inner % sep ))
         (apply str))))

(defrecord InlineCss [css]
  CSS
  (css-compile [this _] css))

(defn inline-css [s]
  (InlineCss. s))