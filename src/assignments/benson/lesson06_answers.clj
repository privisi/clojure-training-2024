(ns assignments.benson.lesson06-answers
  (:require [clojure.java.io :as io]))

(defn grep
  "A unix-grep lookalike.  Return a vector of each line in FILE
  which contains one or more matches of the supplied REGEX-STRING."
  [file regex-string]
  (let [re (re-pattern regex-string)]
    (with-open [rdr (io/reader file)]
      (doall
       (filter (partial re-find re) (line-seq rdr))))))

(grep "src/assignments/benson/inputday1.txt" #"\+")

(defn lazy-grep
  "A unix-grep lookalike.  Return a lazy seq of each line in FILE
  which contains one or more matches of the supplied REGEX-STRING.

  And... problem?"
  [file regex-string]
  (let [rdr (io/reader file)]
    (letfn [(match-line? [line] (re-find (re-pattern regex-string) line))
            (proceed []
              (let [line (.readLine rdr)]
                (println "A")
                (cond
                  (nil? line)        (do (.close rdr) nil)
                  (match-line? line) (cons line (lazy-seq (proceed)))
                  :else              (lazy-seq (proceed)))))]
      (proceed))))

(def some-lines (lazy-grep "src/assignments/benson/inputday1.txt" #"\+"))

(take 10 some-lines)