(ns assignments.PAUL-OMAKI.assignment08
  (:require [clojure.string :as str])
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout sliding-buffer dropping-buffer]]))


;; Revisit the problem of returning the first hits from multiple search engines, 
;; this time using core.async constructs instead of futures and promises. (You’ll need to use the alts! macro). 
;; Compare and contrast the two approaches.


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

(defn string-good-words-only-checker
  "Checks a string against a list of words. If any of the words on the list are found in the string, returns `false`."
  [list string]
  (not-any? true? (map #(str/includes? string %) list)))

(defn filter-out-strings-containing-these
  "Filters a list of strings against another list of words, and removes any that are found."
  [filter-list coll]
  (filter #(string-good-words-only-checker filter-list %) coll))

(def search-buffer
  "The default search buffer. "
  (chan (dropping-buffer 50)))


(defn process-url-glob
  "Takes a collection of strings (hopefully containing URLs) and filters out unwanted "
  [input]
  (->> input
       (filter-out-strings-containing-these unwanted-url-keywords)
       (set)
       (vec)
       (sort)))

(defn search
  "Searches the Internet for the designated query. Engines supported:
  \n`:bing` `:google` `:yandex` `:brave` `:yahoo` `:mojeek`
  \nExample search: `search :bing \"eggs\"`"
  [engine query]
;;   (>!! search-buffer (vec (fetch-and-extract-all-urls engine query)))
  (>!! search-buffer (fetch-and-extract-all-urls engine query))
  (go (let [result (alts! [search-buffer (timeout 5000)])] ; Timeout after 5 seconds   
        (process-url-glob (first result))
        [:timeout "Search timed out"])))

;;   (go (println (<! search-buffer)))
;;   (>!! search-buffer (fetch-and-extract-all-urls :bing "eggs"))

;;   (search :bing "angry")
;; (process-url-glob (first (alts!! [search-buffer (timeout 5000)])))