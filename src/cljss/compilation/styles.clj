(ns cljss.compilation.styles)

(def compressed-style
  {:indent ""
   :property-separator ""
   :rules-separator ""
   :start-properties ""
   :general-indent ""})

(def classic-style
  {:indent "  "
   :property-separator \newline
   :rules-separator \newline
   :start-properties \newline
   :general-indent ""})