(ns clojure-training-2024.lesson07
  (:require
   [camel-snake-kebab.core :as csk]
   [cheshire.core :as json]
   [clj-http.client :as client]
   [medley.core :as m]))

;; See https://github.com/dakrone/clj-http

;; https://httpbin.org
#_
(client/get "http://example.com")     ;; Just the website HTML etc.
#_
(client/get "http://httpbin.org/get") ;; API call
;; It gives us a bunch of json like stuff
#_
(client/get "http://httpbin.org/get"
            {:as :json})

;; same thing
#_
(-> (client/get "http://httpbin.org/get")
    (:body)
    (json/parse-string csk/->kebab-case-keyword))

#_
(:body (client/get "http://httpbin.org/stream-bytes/5"))

#_
(-> (client/post "https://httpbin.org/post"
                 {:as :json
                  :query-params {:foo "bar"}})
    (:body))

;; Sign up for https://www.pipedrive.com/ dev account
;; https://developers.pipedrive.com/docs/api/v1

;; my pipedrive api
(def api-key "f7344dbee66bc3f1681a10aad85d9e6e07ac2c5a")

#_
(-> (client/get
     "https://a49.pipedrive.com/v1/persons"
     {:as :json
      :query-params {:api_token "f7344dbee66bc3f1681a10aad85d9e6e07ac2c5a"}})
    (:body))

#_
(-> (client/post
     "https://a49.pipedrive.com/v1/persons"
     {:as :json
      :throw-exceptions false
      :query-params {:api_token "f7344dbee66bc3f1681a10aad85d9e6e07ac2c5a"}
      :form-params  (m/map-keys csk/->snake_case_keyword {:name "baul"
                                                          :email "foo@bar.com"
                                                          :phone "99999999"})}))