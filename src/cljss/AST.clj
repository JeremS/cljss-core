;; ## AST
;; When rules are parsed, the result is an AST.
;; We define here the different kinds of nodes this AST can have.

(ns ^{:author "Jeremy Schoffen."}
  cljss.AST
  (:require [clojure.string :as string])
  (:use cljss.protocols
        cljss.selectors.protocols
        cljss.precompilation.protocols))

;; helpers

(declare compile-property-map)

(defn- compile-css [sel inner { start :start-properties
                                  sep :rules-separator
                               indent :outer-indent}]
  (str indent sel " {" start
              inner
       indent "}"))

(defn make-indent [n unit]
  (apply str (repeat n unit)))

;; ### Rule
;; A Rule represent a css rule in the sense `sel {properties: val}`.

(defrecord Rule [selector properties sub-rules]
  Tree
  (children [_] sub-rules)
  (assoc-children [this children]
    (assoc this :sub-rules children))

  CSS
  (empty-rule? [_] (not (or (seq properties)
                        (seq sub-rules))))
  (css-compile [this {start :start-properties
                      unit :indent-unit
                      :as style}]
    (let [d (:depth this)
          outer (make-indent d unit)
          inner (make-indent (inc d) unit)
          new-style (assoc style :outer-indent outer :inner-indent inner)
          compiled-selector (compile-as-selector selector new-style)
          compiled-properties (compile-property-map properties new-style)]
      (compile-css compiled-selector compiled-properties new-style))))

;; Constructor for rules

(defn rule
  ([selection ]
   (rule selection {}))
  ([selection properties]
   (rule selection properties []))
  ([selection properties sub-rules]
   (Rule. selection properties sub-rules)))


;; ### Media Query

(defrecord Query [selector body properties sub-rules]
  Tree
  (children [_] sub-rules)
  (assoc-children [this children]
    (assoc this :sub-rules children))

  CSS
  (empty-rule? [_] (not (or (seq properties)
                            (seq sub-rules))))
  (css-compile [this {start :start-properties
                       unit :indent-unit
                        sep :rules-separator
                      :as style}]
    (let [d (:depth this)
          outer (make-indent d unit)
          inner (make-indent (inc d) outer)
          new-style (assoc style :outer-indent outer :inner-indent inner)
          sel (str "@media " (compile-as-selector selector new-style))
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


;; ### Inline CSS
;; We can use strings as inline css in the DSL,
;; the inlined text has its own AST node.

(defrecord InlineCss [css]
  CSS
  (empty-rule? [_] false)
  (css-compile [this _] css))

(defn inline-css [s]
  (InlineCss. s))


;; ### Css Comments
;; Like inline css we can have comments, the difference
;; with the previous type is that css comments can
;; be suppressed from the compiled result, in function of the
;; value of a compilation option.

(defrecord CssComment [css]
  CSS
  (empty-rule? [_] false)
  (css-compile [this {c :comments}]
    (if c
      (str "/* " css " */")
      "")))

(defn css-comment [s]
  (CssComment. s))
