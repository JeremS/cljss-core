(ns cljss.selectors.types
  (:require cljss.selectors.basic)
  (:import [cljss.selectors.basic 
            Children Siblings GSiblings])
  (:use cljss.selectors.protocols))



(def neutral-t     ::neutral)
(def sel-t         ::sel)
(def simple-t      ::simple-sel)
(def combination-t ::combination)
(def descendant-t  ::descendant)
(def set-t         ::set)

(derive simple-t      sel-t)
(derive combination-t sel-t)
(derive set-t         sel-t)


(derive cljss.selectors.basic.Children  combination-t)
(derive cljss.selectors.basic.Siblings  combination-t)
(derive cljss.selectors.basic.GSiblings combination-t)
(derive descendant-t                    combination-t)


(derive clojure.lang.PersistentVector descendant-t)


(derive String                      simple-t)
(derive clojure.lang.Keyword        simple-t)
(derive clojure.lang.IPersistentSet set-t)

(defn selector-type [sel]
  (if (neutral? sel) 
    neutral-t
    (type sel)))