(ns assignments.PAUL-OMAKI.assignment02)

;;;; 2-1 My Filter

;; filter items implemented via loop
(defn my-filter [function coll]
  (loop [filtered-items []
         remaining-items coll]
    (if (seq remaining-items)
      (let [item-to-check (first remaining-items)
            new-items     (next  remaining-items)]
        (if (function item-to-check)
          (recur (conj filtered-items item-to-check) new-items)
          (recur filtered-items new-items)))
      filtered-items)))



(my-filter odd? [1 2 3 4 5 6 7 8 9 10])


;; filter items implemented via reduce

(defn fn-check-item [function]
  (fn [filtered-items item]
    (if (function item)
      (conj filtered-items item)
      filtered-items)))

(defn my-filter-2 [function coll]
  (reduce (fn-check-item function) [] coll))



(my-filter-2 odd? [1 2 3 4 5 6 7 8 9 10])

;; example items to check
(def hardware-store-items
  [{:item-name "Screws"
    :size "Small"
    :quantity 100
    :price-per-unit 0.05}

   {:item-name "Nails"
    :size "Medium"
    :quantity 50
    :price-per-unit 0.08}

   {:item-name "Bolts"
    :size "Large"
    :quantity 30
    :price-per-unit 0.15}

   {:item-name "Washers"
    :size "Small"
    :quantity 200
    :price-per-unit 0.02}

   {:item-name "Saw Blades"
    :size "Large"
    :quantity 10
    :price-per-unit 2.5}])

(defn large? [item]
  (= (:size item) "Large"))

(my-filter large? hardware-store-items)

(my-filter-2 large? hardware-store-items)


;;;; 2-2 My Update-in

;; example record
(def school-records {1001 {:name    "John Bombadil"
                           :type    :student
                           :classes #{:history :math :art}
                           :grades  [43 65 23 43 56]}
                     1002 {:name    "Ross Burn"
                           :type    :student
                           :classes #{:sports :art :english :math}
                           :grades  [75 59 67 34]}
                     2003 {:name    "Harris Twood"
                           :type    :teacher
                           :salary  50000}})
(defn sort-string [str1]     ;; here is the function we will be applying
  (apply str (sort (remove #{\space} str1))))
;; (defn sort-string [str1]
;;   (apply str (sort str1)))

(defn my-update-in [map key-sequence function & args]
  (loop [current-keys key-sequence
         next-keys (rest key-sequence)
         current-map map]
    (if next-keys 
     (recur next-keys (rest next-keys) (current-map))
      (update current-map (first current-keys) #(apply function % args)))))


(defn my-update-in-2 [map key-sequence function & args]
  (assoc-in map key-sequence (#(apply function % args) (get-in map key-sequence))))

(update-in school-records [ 2003 :name ] sort-string)

(my-update-in school-records [2003 :name] sort-string)
(my-update-in-2 school-records [2003 :name] sort-string)


;;;; 3. Write a function which solves this problem: https://4clojure.oxal.org/#/problem/77

;;; defs

(defn find-anagram-sets [set-name]
  (group-by sort-string (map (fn [x] (identity x)) set-name)))
(defn find-anagrams-in-set [set-name]
  (set (filter #(> (count %) 1) (vals (find-anagram-sets set-name)))))


;;; example sets
(def eat-set ["meat" "mat" "team" "mate" "eat"])
(def veer-set ["veer" "lake" "item" "kale" "mite" "ever"])


;;; test solution
(find-anagrams-in-set eat-set)
(find-anagrams-in-set veer-set)


;;;; 4. Write a function which solves this problem: https://4clojure.oxal.org/#/problem/53

;; (defn group-ascending [vector]
;;     (loop [collected []                ;; empty collection to put result in
;;            remaining vector            ;; what's left
;;            prev-coll []]   
;;       (println collected remaining prev-coll);; storage for the previous set
;;       (if (seq remaining)     ;; continue as long as the remaining amount is not empty
;;        
;;  ;fix       (if (> (second remaining) (first remaining)) ;; check if the next number is higher than the previous one
;;           (recur (conj collected (first remaining)) (rest remaining) prev-coll)
;;           (recur collected (rest remaining) prev-coll))
;;         collected))) ;; return map of what has been collected

(defn safe-<= [a b]
  (if (and (not (nil? a)) (not (nil? b)))
    (<= a b)
    "nil"))

(defn safe-> [a b]
  (if (and (not (nil? a)) (not (nil? b)))
    (> a b)
    "nil"))

(defn return-blank-if-count-less-than [num output]
  (if (< num (count output)) output []))
(defn return-item-with-highest-count [vec1 vec2]
  (if (< (count vec1) (count vec2)) vec2 vec1))

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

(count [1 0 1 2 3 0 4 5])

(= (group-ascending [1 0 1 2 3 0 4 5]) [0 1 2 3])

(group-ascending [1 0 1 2 3 0 4 5])

(= (group-ascending [5 6 1 3 2 7]) [5 6])

(= (group-ascending [2 3 3 4 5]) [3 4 5])

(= (group-ascending [7 6 5 4]) [])


(< (count (return-item-with-highest-count [4] [5])) 2)
