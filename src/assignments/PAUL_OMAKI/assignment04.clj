(ns assignments.PAUL-OMAKI.assignment04
  (:require [clojure.string :as str]))


;;    Write a function that takes a string as an argument and searches for it on Bing and Google using the slurp function. Your function should return the HTML of the first page returned by the search.
;;;     => Google returns 403 forbidden, so excluding Google for now
(defn fetch-bing-search
  "Grabs the entire HTML contents of the Bing page searching for a specified `query`."
  [query]
  (let [url (str "http://www.bing.com/search?q=" query)]
    (slurp url)))

(defn bing-search [query]
  (println "Searching Bing for " query)
  (future (fetch-bing-search query)) 30000 "Timed out.")

(bing-search "eggs")


;;    Update your function so it takes a second argument consisting of the search engines to use.

(def search-engine-urls {:bing    {:name "Bing"   :url "http://www.bing.com/search?q="}
                         :google  {:name "Google" :url "https://www.google.com/search?client=firefox-b-d&"}
                         :yandex  {:name "Yandex" :url "https://yandex.com/search/?text="}
                         :brave   {:name "Brave"  :url "https://search.brave.com/search?q="}
                         :yahoo   {:name "Yahoo"  :url "https://search.yahoo.com/search?p="}
                         :mojeek  {:name "Mojeek" :url "https://www.mojeek.com/search?q="}})

(defn get-search-string-base
  "Converts the keyword of any of the search engines in the map to the base component of their search URL."
  [engine]
  (get-in search-engine-urls [engine :url]))

(defn fetch-html-from-search-url
  "Grabs the entire HTML contents of a specified search engine page searching for a specified `query`."
  [engine query]
  (let [engine-url (get-search-string-base engine)
        url (str engine-url query)]
    (slurp url)))


;;    Create a new function that takes a search term and search engines as arguments, and returns a vector of the URLs from the first page of search results from each search engine.

(def url-regex #"\b(?:https?|ftp):\/\/[-A-Za-z0-9+&@#\/=%?\\-_.:;]*[-A-Za-z0-9+&@#\/=%\\-_\\?]") ; Regex to get non-malformed URLs.

;; We want our search results to get useful content, so these are words I don't want to see in the URLs.
(def unwanted-url-keywords   #{"yahoo"
                               "google"
                               "yandex"
                               "w3.org"
                               "&quot"
                               "translate.ru"
                               "yastatic.net"
                               "bing.net"
                               "yimg.com"
                               "brave.com"
                               "bing.com"
                               "mojeek.com"})


(defn extract-urls
  "Returns a lazy sequence of all the URLs extracted from an input chunk of text."
  [text]
  (re-seq url-regex text))

(defn fetch-and-extract-all-urls
  "Uses `extract-url` on a chunk of HTML retrieved from a search engine."
  [engine query]
  (extract-urls (fetch-html-from-search-url engine query)))

;; (defn remove-duplicates
;;   "Sorts output, then removes exact duplicates found."
;;   [output]
;;   (map first (partition-by identity (sort output))))

(defn keyword-checker
  "Checks a string against a list of keywords. If any keywords are found, returns `false`."
  [list string]
  (not-any? true? (map #(str/includes? string %) list)))

(defn filter-out-strings-containing-these
  "Filters a list of strings against another list of words, and removes any that are found."
  [filter-list coll]
  (filter #(keyword-checker filter-list %) coll))


(defn search
  "Searches the Internet for the designated query. Engines supported:
\n`:bing` `:google` `:yandex` `:brave` `:yahoo` `:mojeek`
\nExample search: `search :bing \"eggs\"`"
  [engine query]
  (deref (future (->> (fetch-and-extract-all-urls engine query)
                     (filter-out-strings-containing-these unwanted-url-keywords)
                     (set)
                     (vec)
                     (sort))) 
         3000 "Timed out."))





;;    Create an atom with the initial value 0, use swap! to increment it a couple of times, and then dereference it.

(defn nendoroid-life
  "The daily life of a Nendoroid as an atom."
  []
  (def nendoroid (atom {:name "Kyaru"
                        :current-health 40
                        :max-health 40
                        :wear-level 0
                        :replaced-pieces 0}))
  (println @nendoroid)

  (swap! nendoroid update-in [:wear-level] + 2)
  (println @nendoroid)

  (swap! nendoroid (fn [current-state]
                     (merge-with + current-state {:missing-pieces 1 :replaced-pieces 1 :current-health -25})))

  @nendoroid)

(nendoroid-life)


;;    Create a function that uses futures to parallelize the task of downloading random quotes fromhttp://www.braveclojure.com/random-quote using (slurp "http://www.braveclojure.com/random-quote"). The futures should update an atom that refers to a total word count for all quotes. The function will take the number of quotes to download as an argument and return the atom’s final value. Keep in mind that you’ll need to ensure that all futures have finished before returning the atom’s final value. Here’s how you would call it and an example result:

(defn fetch-lorem-ipsum []
  (slurp "https://www.lipsum.com/feed/html"))
(fetch-lorem-ipsum)




(defn mock-api-call []
  (Thread/sleep (rand-int 3000)))

(defn get-quote-from-lipsum []
 ; (mock-api-call) ;; this isn't working properly here or in the thread???
 (rand-nth ["'Lorem ipsum.' - Dotor L." "'Consectetur adipiscing elit.' - Dotor L." "'Duis aute irure dolor.' - Dotor L." "'Excepteur sint occaecat cupidatat non proident.' - Dotor L."]))


(defn get-quotes
  "Gets several very important quotes. Returns a vector."
  []
  (let [quote1 (future (Thread/sleep 3000) (get-quote-from-lipsum))
        quote2 (future (Thread/sleep 2000) (get-quote-from-lipsum))
        quote3 (future (Thread/sleep 1999) (get-quote-from-lipsum))
        quote4 (future (Thread/sleep 1000) (get-quote-from-lipsum))
        quote-collection []]
    (conj quote-collection 
          (deref quote1 2000 "Timed out.")
          (deref quote2 2000 "Timed out.")
          (deref quote3 2000 "Timed out.")
          (deref quote4 2000 "Timed out."))))

(get-quotes)



;;THIS SEEMS TO BE DOWN, create a function with a random delay of 1-3 seconds instead and use that to simulate a delay.


;;        Create representations of two characters in a game. The first character has 15 hit points out of a total of 40. The second character has a healing potion in his inventory. Use refs and transactions to model the consumption of the healing potion and the first character healing.

(def bonzi-buddy (atom {:name "Bonzi"
                        :current-health 40
                        :max-health 40
                        :inventory {:health-potion 1
                                    :cool-knife 2
                                    :hammock 1}}))



(defn use-potion [bonzi-buddy nendoroid]
  (dosync
   (when (and (< (get @nendoroid :current-health) (get @nendoroid :max-health))
              (> (get-in @bonzi-buddy [:inventory :health-potion]) 0))
         (swap! nendoroid update-in [:current-health] + 25)
         (swap! bonzi-buddy update-in [:inventory :health-potion] - 1))))

(use-potion bonzi-buddy nendoroid)


