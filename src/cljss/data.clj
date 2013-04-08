(ns cljss.data)

(defrecord Rule [selector properties sub-rules])

(defn rule 
  ([selection ]
   (rule selection {}))
  ([selection properties]
   (rule selection properties []))
  ([selection properties sub-rules]
   (Rule. selection properties sub-rules)))


