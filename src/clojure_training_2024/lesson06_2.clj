(ns clojure-training-2024.lesson06-2)

;; Notice the namespace:
;; clojure-training-2024.lesson06-2
;; Corresponds to our PROJECT_NAME.FILE_NAME
;; Also notice:
;; `_` in the filename is converted to `-`


;; (ns clojure-training-2024.lesson06-2-i-dont-exist)
;; Namespaces need to match the specific file, if an equivalent file doesn't exist
;; then the namespace will still work but it is terrible practice.


(defn i-do-something []
  (println (str "Calling from: " *ns*)
           "\nHello!"))

(defn foo []
  2)