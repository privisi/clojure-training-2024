(ns assignments.PAUL-OMAKI.assignment03)

;; problem 128


(defn card-suit-interpreter
  "Accepts standard suit letters (D, H, C, S) of playing card notation and outputs a value that can be interpreted by Clojure. Input should be a single character."
  [char]
  (let [letter-to-test char]
    (case letter-to-test
      \D :diamond    ; If it begins with a D, the suit is diamond, etc.
      \H :heart
      \C :club
      \S :spade
      "Case not handled yet.")))

(defn card-rank-interpreter
  "Accepts rank letters (A, K, Q, J, T) of playing card notation and outputs a value that can be interpreted by Clojure. Input should be a single character."
  [char]
  (let [char-to-test char] 
      (case char-to-test ;; look up case
        \A 12          ; Named cards and 10.
        \K 11
        \Q 10
        \J 9
        \T 8 
        (- (Integer/parseInt (str char-to-test)) 2) ; If input is not one of the above letters, it must be a number.
        )))

(defn card-converter
  "Takes the first and last characters of an input string and attempts to convert it into a map including the card's data.
   \n
   \nUsage:
   \n(card-converter H3) => {:suit :heart :rank 3}"
  [input]
  (assoc {} :suit (card-suit-interpreter (first input)) :rank (card-rank-interpreter (last input))))



;; Tests for above code.

(= {:suit :diamond :rank 10} (card-converter "DQ"))

(= {:suit :heart :rank 3} (card-converter "H5"))

(= {:suit :club :rank 12} (card-converter "CA"))

(= (range 13) (map (comp :rank card-converter str)
                   '[S2 S3 S4 S5 S6 S7
                     S8 S9 ST SJ SQ SK SA]))



;; problem 178, uses 128 as a base

(defn hand-converter
  "Maps card-converter on every card in a given hand."
  [hand]
  (map card-converter hand))

(defn rank-frequencies
  "Checks how many occurrences of each rank appear in a given hand."
  [hand]
  (frequencies (map :rank (hand-converter hand))))

(defn suit-frequencies
  "Checks how many occurrences of each suit appear in a given hand."
  [hand]
  (frequencies (map :suit (hand-converter hand))))

(defn rank-count
  "Returns true if there are [x] cards with the same rank in a given hand."
  [x hand]
  (println (map second (rank-frequencies hand)))
  (some #(= x %) (map second (rank-frequencies hand))))

(defn suit-count
  "Returns true if there are [x] cards with the same suit in a given hand."
  [x hand]
  ;(println (map second (suit-frequencies hand)))
  (some #(= x %) (map second (suit-frequencies hand))))

(defn two-pair-tester
  "Tests to see if there are multiple pairs in one hand. Returns true if 2 pairs are found."
  [hand]
  (some #(= 2 %) (map second (frequencies (map second (rank-frequencies hand))))))

(defn card-rank-sorter
  "Puts the ranks of the cards of a given hand in order for further processing."
  [hand]
  (sort (map :rank (hand-converter hand))))

(defn sorted-aces-low-hand 
  "Sorts hand, then subtracts from the last value of the hand and appends it to the front, simulating an \"aces low\" hand.  
   It doesn't actually matter if the last card is an ace or not, because the next step only looks for a contiguous sequence."
  [hand]
  (conj (butlast (card-rank-sorter hand)) (- 11 (last (card-rank-sorter hand)))))

(defn contiguous-number-checker
  "Checks to see if the numbers in a given set are contiguous or not."
  [nums]
  (every? #(= % 1) (map (fn [a b] (- a b)) (rest nums) nums)))

(defn ascending-rank-checker
  "Checks to see if the ranks of a hand are contiguous or not. Returns true or false."
  [hand] 
  (let [sorted-ranks (card-rank-sorter hand)
        sorted-low-ranks (sorted-aces-low-hand hand)]
     (or (contiguous-number-checker sorted-ranks) (contiguous-number-checker sorted-low-ranks))))


;;; The main function of this exercise.

(defn poker-hand-categorizer
  "Takes a given poker hand in playing card notation and categorizes it.
   \n
   \n Usage:
   \n(poker-hand-categorizer [\"HA\" \"DK\" \"HQ\" \"HJ\" \"HT\"]) => :straight"
  [hand]
  (cond
    (and (suit-count 5 hand) (ascending-rank-checker hand)) :straight-flush
    (rank-count 4      hand)      :four-of-a-kind
    (and (rank-count 3 hand) (rank-count 2 hand))           :full-house
    (suit-count 5      hand)      :flush
    (ascending-rank-checker hand) :straight
    (rank-count 3      hand)      :three-of-a-kind
    (two-pair-tester   hand)      :two-pair
    (rank-count 2      hand)      :pair
    :else                         :high-card))


;; Tests for above code.

(= :high-card       (poker-hand-categorizer ["HA" "D2" "H3" "C9" "DJ"]))
(= :pair            (poker-hand-categorizer ["HA" "HQ" "SJ" "DA" "HT"]))
(= :two-pair        (poker-hand-categorizer ["HA" "DA" "HQ" "SQ" "HT"]))
(= :three-of-a-kind (poker-hand-categorizer ["HA" "DA" "CA" "HJ" "HT"]))
(= :straight        (poker-hand-categorizer ["HA" "DK" "HQ" "HJ" "HT"]))
(= :straight        (poker-hand-categorizer ["HA" "H2" "S3" "D4" "C5"]))
(= :flush           (poker-hand-categorizer ["HA" "HK" "H2" "H4" "HT"]))
(= :full-house      (poker-hand-categorizer ["HA" "DA" "CA" "HJ" "DJ"]))
(= :four-of-a-kind  (poker-hand-categorizer ["HA" "DA" "CA" "SA" "DJ"]))
(= :straight-flush  (poker-hand-categorizer ["HA" "HK" "HQ" "HJ" "HT"]))
















;; (defn card-suit-interpreter [placement input]
;;   (let [char-to-test (placement (str input))] ; get the first character of the input
;;     (cond
;;       (= \D char-to-test) :diamond        ;; if it begins with a D, the suit is diamond
;;       (= \H char-to-test) :heart       
;;       (= \C char-to-test) :club        
;;       (= \S char-to-test) :spade       
;;       (or \A \J \Q \K int) (fn [] card-rank-interpreter)  ;; if 
;;       :else "Case not handled yet.")))

;; (defn card-rank-interpreter [placement input]
;;   (let [char-to-test (last (str input))] ; get the first character of the input
;;     (println char-to-test)
;;     (cond
;;       (= \A char-to-test) 12
;;       (= \K char-to-test) 11
;;       (= \Q char-to-test) 10
;;       (= \J char-to-test) 9

;;       (or \D \H \C \S) card-suit-interpreter
;;       :else "Case not handled yet.")))

;; (defn number-to-rank
;;   "[x]
;;    Accepts numbers of standard playing card notation and outputs a value reduced by 2."
;;   [num]
;;   (let [num-to-test num]
;;     (- char-to-test 2)))

;; (defn card-rank-interpreter
;;   "[x]
;;    Determines whether the input is a letter or number, then calls the appropriate function to output an integer related to the rank of the card."
;;   [input]
;;   (if (int? input)
;;     (number-to-rank input)
;;     (letter-to-rank input)))


;; These functions were originally split but here's a test of everything integrated
;; (defn card-suit-rank-interpreter [placement input]
;;   (let [char-to-test (placement (str input))] ; get one of the characters of the input
;;     (println char-to-test)
;;     (cond
;;       (= \D char-to-test) :diamond    ; if it begins with a D, the suit is diamond, etc.
;;       (= \H char-to-test) :heart
;;       (= \C char-to-test) :club
;;       (= \S char-to-test) :spade
;;       (= \A char-to-test) 12          ; hardcoding these values
;;       (= \K char-to-test) 11
;;       (= \Q char-to-test) 10
;;       (= \J char-to-test) 9
;;       (= \T char-to-test) 8
;;       (int? (int char-to-test)) (- (int char-to-test) 2)

;;       :else "Case not handled yet.")))

;; flawed implementation, it is converting the numbers to string and getting the character value back instead of the raw number



;; (defn pair-tester [hand]
;;   (some #(= 2 %) (map second (rank-frequencies hand))))