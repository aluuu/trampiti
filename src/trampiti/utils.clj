(ns trampiti.utils)

(defn query-string [params]
  (clojure.string/join
   "&"
   (for [[k v] params]
     (format "%s=%s" k v))))
