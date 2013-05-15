;; Precompilation protocols

(ns cljss.precompilation.protocols)

(defprotocol Tree
  (children [this]
    "Each node of the AST must return the keyword to access its children.")
  (assoc-children [this new-children]
    "Returns a new version of the tree with the new children"))