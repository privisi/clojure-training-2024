(ns assignments.PAUL-OMAKI.Advent2018.day-1 
  (:require [clojure.string :as str]))

(def input 
  (str/split-lines (slurp "src/assignments/PAUL_OMAKI/Advent2018/day1input.txt")))

;; Day 1 part 1

(defn extract-integers [strings]
  (map read-string strings))

(reduce + (extract-integers input))


;; Day 1 part 2

(defn find-first-duplicate
  "Performs an operation to find the first duplicate total in the set."
  [ints]
  (loop [int-list   ints
         total       0
         past-totals [ ]]
    (println past-totals)
    (let [first-int (first int-list)]
      (if (contains? past-totals (+ first-int total))
        (+ first-int total)
        (recur (rest int-list) (+ first-int total) (conj past-totals (+ first-int total)))))))

(find-first-duplicate (extract-integers input))


