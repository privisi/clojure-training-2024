(ns assignments.benson.advent-day1
  (:require [clojure.string :as string]))

(def input (slurp "src/assignments/benson/inputday1.txt"))

(defn- ->numbers
  "Takes an INPUT which is a string containing numbers separated by new lines
   and converts it into a collection of integers"
  [input]
  (map read-string (string/split-lines input)))

(defn resulting-frequency
  "Takes an INPUT which is a string containing numbers separated by new lines
   and returns the total of all numbers"
  [input]
  (->> input
       (->numbers)
       (reduce +)))

(resulting-frequency input)


(defn first-twice-frequency
  "Takes an INPUT which is a string containing numbers separated by new lines
   and adds each number in the collection individually (and if exhausted repeats with
   the same collection of numbers infinitely) until a total value has occurred twice,
   then returns it."
  [input]
  (loop [[curr-num & rest-nums] (cycle (->numbers input))
         total    0
         occurred #{0}]
    (let [new-total (+ total curr-num)]
      (if (contains? occurred new-total)
        new-total
        (recur rest-nums new-total (conj occurred new-total))))))

(first-twice-frequency input)