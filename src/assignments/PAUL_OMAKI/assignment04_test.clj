(ns assignments.PAUL-OMAKI.assignment04-test
  #_{:clj-kondo/ignore [:refer-all]}
  (:require [clojure.test :refer :all]
            [assignments.PAUL-OMAKI.assignment04 :refer :all]))


(deftest fetch-urls
  (testing
   (testing "Fetch a Bing URL"
     (is (= "<!DOCTYPE html><html dir=\"ltr\" lang=\"ja\" xml:lang=\"ja\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:We"
            (apply str (take 100 (fetch-bing-search "eggs")))))
     (is (= "https://search.brave.com/search?q=" (get-search-string-base :brave))))
    (testing "Searching Yandex"
      (search :yandex "eggs"))
    (testing "Searching Google"
      (search :google "eggs"))
    (testing "Searching Brave"
      (search :brave "eggs"))
    (testing "Searching Yahoo"
      (search :yahoo "eggs"))
    (testing "Searching Bing"
      (search :bing "eggs"))
    (testing "Searching Mojeek"
      (search :mojeek "eggs"))))

