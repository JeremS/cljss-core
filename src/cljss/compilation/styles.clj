(ns cljss.compilation.styles)

(def compressed
  {:indent ""
   :property-separator ""
   :rules-separator ""
   :start-properties ""
   :general-indent ""})

(def classic
  {:indent "  "
   :property-separator \newline
   :rules-separator \newline
   :start-properties \newline
   :general-indent ""})