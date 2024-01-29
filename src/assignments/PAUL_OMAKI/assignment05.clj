(ns assignments.PAUL-OMAKI.assignment05 
  (:require [clojure.string :as str]))


(defn split-lines-from-file
  "Gets all the text lines from a file and outputs them with the `split-lines` function."
  [file-path]
  (->  file-path
       slurp
       (str/split-lines)))

(defn grep
  "Returns lines in a file that match a given regex find value. Returns a vector."
  [file-path regex]
  (->> (split-lines-from-file file-path)
       (filter #(re-seq (re-pattern regex) %))
       vec))

(grep "/tmp/somefile.txt" "[a]")

(grep "/somefile.txt" "bee")





;; 4clojure

;; Problem 32
(defn repeat-elements [elements]
  ((mapcat  #(repeat 2 %) elements)))

(= (repeat-elements [:a :a :b :b]) '(:a :a :a :a :b :b :b :b))

;; Problem 33

(defn repeat-elements 
  ([elements]
   (mapcat  #(repeat 2 %) elements))
  ([elements times]
   (mapcat  #(repeat times %) elements)))

(= (repeat-elements [:a :b] 4) '(:a :a :a :a :b :b :b :b))

(= (repeat-elements [[1 2] [3 4]] 2) '([1 2] [1 2] [3 4] [3 4]))


;; Problem 34

(defn my-range [start end]
  (take (- end start) (iterate inc' start)))

(= (my-range 1 4) '(1 2 3))
(= (my-range -2 2) '(-2 -1 0 1))
(= (my-range 5 8) '(5 6 7))


;; problem 38

(defn my-max [& input]
  (reduce #(if (< %1 %2) 
               %2
               %1) input))
(my-max 1 8 3 4)

;; problem 39

(defn my-interleave [seq1 seq2]
  (mapcat vector seq1 seq2))

(my-interleave [1 2 3] [:a :b :c]) '(1 :a 2 :b 3 :c)
(my-interleave [1 2 3 4] [5])


;; problem 40

(defn my-interpose [to-join seq]
  (loop [output [] 
         rem-seq seq]
    (println output)
    (if (any? rem-seq)
      (recur (conj output (first rem-seq) to-join) (rest rem-seq))
      output)))

(= (my-interpose 0 [1 2 3]) [1 0 2 0 3])

(my-interpose 0 [1 2 3])

