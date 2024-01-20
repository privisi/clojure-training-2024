(ns clojure-training-2024.lesson03
  (:require [clojure.java.jdbc :as jdbc]))


;; Reviewing Pauls solution for 4clj problem 53 (get longest subseq)

;; These "safe" solutions are ridiculous and show that there is some sort of flaw in the code
;; that is trying to be guarded against
(defn safe-<= [a b]
  (if (and (not (nil? a)) (not (nil? b)))
    (<= a b)
    true))

(defn safe-> [a b]
  (if (and (not (nil? a)) (not (nil? b)))
    (> a b)
    true))

;; The if statement needs to be indented
;; It says return blank but it returns []
(defn return-blank-if-count-less-than [num output]
  (if (< num (count output)) output []))

;; It says return item, but it's returning a vector
(defn return-item-with-highest-count [vec1 vec2]
  (if (< (count vec1) (count vec2)) vec2 vec1))

;; The indenting in recur is off and confusing
#_
(recur (return-item-with-highest-count prev-best collected) ;; make our previous best into a mirror of the higher of the 2 counts
       (rest remaining)
       (conj collected number-to-check))

;; last-in-collection, should have the context that it's a number
;; nil? exists can do (nil? (= nil (seq remaining))),
;; better yet, just call (empty? remaining)
(defn group-ascending [vector]
  (loop [prev-best []                ;; the collection with the highest count so far
         remaining vector            ;; what's left to count
         collected []]              ;; empty collection to put current result in
    (let [number-to-check (first remaining)
          last-in-collection (last collected)]
         (cond                                            ;; if nothing is remaining, return the best one
           (= nil (seq remaining)) (return-blank-if-count-less-than 1 (return-item-with-highest-count prev-best collected)) ;; return value, disqualify if less than 2 items in max sequence
           (safe-> number-to-check last-in-collection) (recur  ;; if next number in sequence is higher
                                                   (return-item-with-highest-count prev-best collected) ;; make our previous best into a mirror of the higher of the 2 counts
                                                   (rest remaining)
                                                   (conj collected number-to-check))
           (safe-<= number-to-check last-in-collection) (recur  ;; if next number is lower
                                                   (return-item-with-highest-count prev-best collected) ;; make our previous best into a mirror of the higher of the 2 counts
                                                   (rest remaining)
                                                   [number-to-check])  ;; return an empty collection to start over

           :else (return-item-with-highest-count prev-best collected)))))



;; Solution for anagram

;;; Break smaller tasks into functions
(defn sort-string [str1]
  (apply str (sort str1)))

(defn find-anagram-sets [set-name]
  (vals
   (group-by sort-string (map (fn [x] (identity x)) set-name))))

(defn find-anagrams-in-set [set-name]
  (set
   (filter #(> (count %) 1) (find-anagram-sets set-name))))


;;; example sets
(def eat-set ["meat" "mat" "team" "mate" "eat"])
(def veer-set ["veer" "lake" "item" "kale" "mite" "ever"])


;;; test solution
(find-anagrams-in-set eat-set)
(find-anagrams-in-set veer-set)


;; Another solution by Alain (with docstrings)

(defn anagram?
  "Return true IF the words A and B are anagrams of one another."
  [a b]
  (= (sort a)
     (sort b)))

(defn add-anagram [m w]
  (let [[match] (filter (partial anagram? w) (keys m))]
    (if match
      (update m match conj w)
      (assoc m w #{w}))))


;; I'm not completely happy with the above, so now
;; I'd add a docstring and tweak the code for readability
;; Careful! In this function I shadowed the symbol `map`.
;; The `map` passed in is received as a hash map value, NOT
;; as the clojure.core/map function.
;; This is the result of clojure being what is called a "LISP-1".
;;
(defn add-anagram
  "Find which key of MAP, if any, is an anagram of WORD,
  and add WORD to the set of current anagrams under that key.
  If it's not an anagram of any key so far, add it to MAP
  under WORD as key with WORD being the only anagram in the set so far.

  The incoming map and resulting structure have this form:
  {\"team\" #{\"team\" \"mate\"}
   \"mite\" #{\"mite\"}}"
  [map word]
  (if-let [match (first (filter (partial anagram? word) (keys map) ))]
    (update map match conj word)
    (assoc map word #{word})))

;; Solution
(defn anagrams-problem-77
  "From a sequence of WORDS, output a set containing
   sets of anagrams found in the sequence.
   See http://www.4clojure.com/problem/77"
  [words]
  (->> (reduce add-anagram {} words)
       vals
       (filter #(> (count %) 1)) ; Spec only wanted collections of 2 or more anagrams
       set))


;;;; Implement `filter`

;; with reduce

(defn filter-with-reduce [pred coll]
  (reverse
   (reduce (fn [filtered-coll x]
             (if (pred x)
               (conj filtered-coll x)
               filtered-coll))
           (seq [])
           coll)))

(filter-with-reduce odd? [1 2 3 4 5 6 7])

;; not lazy
(type (filter-with-reduce odd? [1 2 3 4 5 6 7])) ;; clojure.lang.PersistentList

;; How do we do it the lazy way?

(defn our-lazy-filter [pred coll]
  (let [[x & more] coll]
    (when x
      (if (pred x)
        (cons x (lazy-seq (our-lazy-filter pred more)))
        (lazy-seq (our-lazy-filter pred more))))))

(our-lazy-filter odd? [1 2 3 4 5 6 7])

;; this is lazy, it's returning the instructions
(type (our-lazy-filter odd? [1 2 3 4 5 6 7])) ;; clojure.lang.Cons



;;;;


(def db-conn
  {:dbtype "sqlite"
   :dbname "test.db"
   :classname "org.sqlite.JDBC"})

(jdbc/query db-conn ["select 2;"])


(def fruit-table-ddl
  (jdbc/create-table-ddl :fruit
                         [[:name "varchar(32)"]
                          [:appearance "varchar(32)"]
                          [:cost :int]
                          [:grade :real]]))

(defonce table-creation-results
  (jdbc/db-do-commands db-conn
                       [fruit-table-ddl
                        "CREATE INDEX name_ix ON fruit ( name );"]))

(jdbc/insert-multi! db-conn :fruit
                    [{:name "Apple" :appearance "rosy" :cost 24}
                     {:name "Orange" :appearance "round" :cost 49}])


(jdbc/query db-conn
            ["SELECT * FROM fruit WHERE appearance = ?" "rosy"])

(defn find-fruits [appearance]
  (jdbc/query db-conn
              ["SELECT * FROM fruit WHERE appearance = ?" appearance]))

(find-fruits "round")