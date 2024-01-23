(ns assignments.PAUL-OMAKI.assignment03)

;; 4clojure problem 128 


(defn card-suit-interpreter
  "Accepts standard suit letters (D, H, C, S) of playing card notation and outputs a value that can be interpreted by Clojure. Input should be a single character."
  [char]
  (case char
    \D :diamond    ; If it begins with a D, the suit is diamond, etc.
    \H :heart
    \C :club
    \S :spade
    throw (Exception. "Invalid suit, or no suit supplied to function.")))

(defn card-rank-interpreter
  "Accepts rank letters (A, K, Q, J, T) of playing card notation and outputs a value that can be interpreted by Clojure. Input should be a single character."
  [char]
  (case char ;; look up case
    \A 12          ; Named cards and 10.
    \K 11
    \Q 10
    \J 9
    \T 8
    (- (Integer/parseInt (str char)) 2))) ; If input is not one of the above letters, it must be a number.

(defn card-converter
  "Takes the first and last characters of an input string and attempts to convert it into a map including the card's data.
   \n
   \nUsage:
   \n(card-converter H3) => {:suit :heart :rank 3}"
  [input]
  (assoc {} :suit (card-suit-interpreter (first input)) :rank (card-rank-interpreter (last input))))



;; 4clojure problem 178, uses 128 as a base


;; Basic utilities for card checks.

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
  (some #(= x %) (map second (rank-frequencies hand))))

(defn suit-count
  "Returns true if there are [x] cards with the same suit in a given hand."
  [x hand]
  (some #(= x %) (map second (suit-frequencies hand))))


;; More in-depth functions for specific hands.

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


;; Individual checks for specific hand combinations.

(defn two-pair?
  "Tests to see if there are multiple pairs in one hand. Returns true if 2 pairs are found."
  [hand]
  (some #(= 2 %) (map second (frequencies (map second (rank-frequencies hand))))))

(defn straight-flush?
  "Tests to see if there are 5 cards of the same suit in hand, and if they are in ascending rank. Returns true if both conditions are fulfilled."
  [hand]
  (and (suit-count 5 hand) (ascending-rank-checker hand)))

(defn full-house?
  "Tests to see if there are multiple sets with the same rank in one hand. Returns true if a set of 3 and a set of 2 are found."
  [hand]
  (and (rank-count 3 hand) (rank-count 2 hand)))

(defn straight?
  "Tests to see if there are 5 cards in ascending rank in hand. Returns true for both aces high and aces low scenarios."
  [hand]
  (ascending-rank-checker hand))


;;; The main function of this exercise.

(defn poker-hand-categorizer
  "Takes a given poker hand in playing card notation and categorizes it.
   \n
   \n Usage:
   \n(poker-hand-categorizer [\"HA\" \"DK\" \"HQ\" \"HJ\" \"HT\"]) => :straight"
  [hand]
  (cond
    (straight-flush? hand) :straight-flush
    (rank-count 4    hand) :four-of-a-kind
    (full-house?     hand) :full-house
    (suit-count 5    hand) :flush
    (straight?       hand) :straight
    (rank-count 3    hand) :three-of-a-kind
    (two-pair?       hand) :two-pair
    (rank-count 2    hand) :pair
    :else                  :high-card))
