(ns assignments.PAUL-OMAKI.assignment03-test
  (:require [clojure.test :refer :all]
            [assignments.PAUL-OMAKI.assignment03 :refer :all]))

(deftest card-interpreter-tests
  (testing
   (testing "Queen of Diamonds"
     (is (= {:suit :diamond :rank 10} (card-converter "DQ"))))

    (testing "Five of Hearts"
      (is (= {:suit :heart :rank 3} (card-converter "H5"))))

    (testing "Ace of Clubs"
      (is (= {:suit :club :rank 12} (card-converter "CA"))))

    (testing "All values of one suit."
      (is (= (range 13) (map (comp :rank card-converter str)
                             '[S2 S3 S4 S5 S6 S7
                               S8 S9 ST SJ SQ SK SA]))))))

(deftest poker-hand-tests
  (testing
   (testing "High card"
     (is (= :high-card       (poker-hand-categorizer ["HA" "D2" "H3" "C9" "DJ"]))))
    (testing "One pair"
      (is (= :pair            (poker-hand-categorizer ["HA" "HQ" "SJ" "DA" "HT"]))))
    (testing "Hand with two pairs"
      (is (= :two-pair        (poker-hand-categorizer ["HA" "DA" "HQ" "SQ" "HT"]))))
    (testing "Three of a kind"
      (is (= :three-of-a-kind (poker-hand-categorizer ["HA" "DA" "CA" "HJ" "HT"]))))
    (testing "Straight with aces high"
      (is (= :straight        (poker-hand-categorizer ["HA" "DK" "HQ" "HJ" "HT"]))))
    (testing "Straight with aces low"
      (is (= :straight        (poker-hand-categorizer ["HA" "H2" "S3" "D4" "C5"]))))
    (testing "Flush"
      (is (= :flush           (poker-hand-categorizer ["HA" "HK" "H2" "H4" "HT"]))))
    (testing "Full house"
      (is (= :full-house      (poker-hand-categorizer ["HA" "DA" "CA" "HJ" "DJ"]))))
    (testing "Four of a kind"
      (is (= :four-of-a-kind  (poker-hand-categorizer ["HA" "DA" "CA" "SA" "DJ"]))))
    (testing "Straight flush"
      (is (= :straight-flush  (poker-hand-categorizer ["HA" "HK" "HQ" "HJ" "HT"]))))))
