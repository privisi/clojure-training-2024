(ns clojure-training-2024.lesson03-test
  (:require [clojure-training-2024.lesson03 :refer :all]
            [clojure.test :refer :all]))

(deftest test-our-filter-with-reduce
  (testing "Testing filter with reduce"
    (is (= '(1 3 5 7) (filter-with-reduce odd? [1 2 3 4 5 6 7])))))

(deftest test-our-filter-with-reduce-with-are
  (testing
   (are [x y] (= x y)
     '(1 3 5 7) (filter-with-reduce odd? [1 2 3 4 5 6 7])
     '(2 4 6)   (filter-with-reduce even? [1 2 3 4 5 6 7])
     '()        (filter-with-reduce even? [1]))))