(ns assignments.PAUL-OMAKI.Advent2018.day-2 
  (:require [clojure.string :as str]))
  
  (def input
    (str/split-lines (slurp "D:/Projects/Coding/clojure-training-2024/src/assignments/PAUL_OMAKI/Advent2018/day2input.txt")))
  
  ;; Day 2 part 1

(def sorted-input (mapv sort (sort input)))
      
  
;; (vec)(partition-by identity ( first (mapv (vec (partition-by identity)) sorted-input)))
;;       (-> input
;;           sort
;;           map )