(ns assignments.PAUL-OMAKI.assignment07
  (:require
   [camel-snake-kebab.core :as csk]
   [cheshire.core :as json]
   [clj-http.client :as client]
   [medley.core :as m]))

(def api-key "dc4ed82a3799de9ad36253efd38595f21383bc49")
(def base-url "https://paul-sandbox5.pipedrive.com/v1/")
(def persons-url       (str base-url "persons"))
(def organizations-url (str base-url "organizations"))
(def leads-url         (str base-url "leads"))
(def test-person-params {:name "John Egbert"
                         :email "stuck@home.co.nz"
                         :phone "4134134134"})
(def test-org-params {:name "Test McTesterson"
                         :email "foo@bar.com"
                         :phone "99999999"})

;;; Getting data

(defn fetch-data
  "Generic request for data. Applies the hardcoded API token."
  ([url] ; for getting all info from the request
   (-> (client/get url
                            {:as :json
                             :query-params {:api_token api-key}})
                (:body)))
  ([url id-num] ; Pipedrive doesn't do server-side filtering with query params, so doing it this way 
       (-> (client/get
            (format "%s/%s/" url id-num)
            {:as :json
             :query-params {:api_token api-key}})
           (:body))))


;; (defn fetch-person
;;   "Fetches a person's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data persons-url key value))
;; (defn fetch-org
;;   "Fetches an organization's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data organizations-url key value))
;; (defn fetch-lead
;;   "Fetches a lead's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data leads-url key value))



(defn fetch-person-by-id
  "Fetches a person's data that matches supplied ID."
  [id-num]
  (get (fetch-data persons-url id-num) :data))
(defn fetch-org-by-id
  "Fetches an organization's data that matches supplied ID."
  [id-num]
  (get (fetch-data organizations-url id-num) :data))
(defn fetch-lead-by-id
  "Fetches a lead's data that matches supplied ID."
  [id-num]
  (get (fetch-data leads-url id-num) :data))

(defn fetch-all-persons
  "Fetches all people in the database."
  []
  (fetch-data persons-url))
(defn fetch-all-orgs
  "Fetches all organizations in the database."
  []
  (fetch-data organizations-url))
(defn fetch-all-leads
  "Fetches all leads in the database."
  []
  (fetch-data leads-url))


;;; Sending new data

(defn post-data
  "Generic upload for data. Applies the hardcoded API token."
  [url params-map]
  (let [response (client/post url
                               {:as :json
                                :throw-exceptions false
                                :query-params {:api_token api-key}
                                :form-params  (m/map-keys csk/->snake_case_keyword params-map)})]
     (if (= 200 (:status response))
       (:body response)
       {:error "Failed to transfer data." :details (:body response)})))

(defn add-person 
  "Creates a new person in the database with the supplied map."
  [params-map]
  (post-data persons-url params-map))

(defn add-org
  "Creates a new organization in the database with the supplied map."
  [params-map]
  (post-data organizations-url params-map))

(defn add-lead
  "Creates a new lead in the database with the supplied map."
  [params-map]
  (post-data leads-url params-map))


(defn patch-data
  "Request for addition of data. Applies the hardcoded API token."
  ([url id-num params-map]
     (-> (client/patch
                  (format "%s/%s/" url id-num) ; 
                  {:as :json
                   :query-params {:api_token api-key}
                   :form-params  (m/map-keys csk/->snake_case_keyword params-map)})
                 (:body))))

(defn modify-person
  "Modifies a person at supplied ID in the database with the supplied map."
  [id-num params-map]
  (patch-data persons-url id-num params-map))

(defn modify-org
  "Modifies a person at supplied ID in the database with the supplied map."
  [id-num params-map]
  (patch-data organizations-url id-num params-map))

(defn modify-lead
  "Modifies a person at supplied ID in the database with the supplied map."
  [id-num params-map]
  (patch-data leads-url id-num params-map))

;;; Removing data

(defn delete-data
  "Request for deletion of data. Applies the hardcoded API token." 
  ([url id-num] 
     (-> (client/delete
                  (format "%s/%s/" url id-num) ; 
                  {:as :json
                   :query-params {:api_token api-key}})
                 (:body))))

(defn delete-person-by-id
  "Creates a request for deleting a person's data that matches supplied ID."
  [id-num]
  (delete-data persons-url id-num))
(defn delete-org-by-id
  "Creates a request for deleting an organization's data that matches supplied ID."
  [id-num]
  (delete-data organizations-url id-num))
(defn delete-lead-by-id
  "Creates a request for deleting a lead's data that matches supplied ID."
  [id-num]
  (delete-data leads-url id-num))



;;; Reading and formatting retrieved data


(defn strip-person-data
  "Reduces the full map JSON to just name, phone, email, company, and their deal count."
  [person]
  (let [primary-phone (first (filter #(= (:primary %) true) (:phone person)))
        org-name (get-in person [:org_id :name])]
    {:id        (:id                 person)
     :name      (:name               person)
     :phone     (get primary-phone   :value)
     :email     (:primary_email      person)
     :company   org-name
     :deals     (:closed_deals_count person)}))
(defn strip-all-persons-data []
  (map strip-person-data (get (fetch-all-persons) :data)))

(defn strip-org-data
  "Reduces the full map JSON to just name, address, postal code, country, and related deals."
  [org] 
  {:id                   (:id                 org)
   :name                 (:name               org)
   :address              (:address            org)
   :postal-code          (:address_postal_code org)
   :country              (:address_country    org)
   :deals                (:closed_deals_count org)})
(defn strip-all-organizations-data []
  (map strip-org-data (get (fetch-all-orgs) :data)))

(defn strip-lead-data
  "Reduces the full map JSON to just name, phone, email, company, and their deal count."
  [lead]
  (let [deal-amount (get-in lead [:value :amount])
        deal-currency (get-in lead [:value :currency])]
  {:organization-id      (:organization_id      lead)
   :title                (:title               lead)
   :amount               deal-amount
   :currency             deal-currency
   :expected-close-date  (:expected_close_date lead)
   :contact              (get (fetch-person-by-id (get lead :person_id)) :name)}))
(defn strip-all-leads-data []
  (map strip-lead-data (get (fetch-all-leads) :data)))





(fetch-all-persons)
(add-person test-person-params)
(fetch-person-by-id 2)
(strip-person-data (fetch-person-by-id 3))
(fetch-org-by-id 1)
(strip-org-data (fetch-org-by-id 2))
(strip-all-persons-data)
(strip-all-organizations-data)
(strip-all-leads-data)
(modify-person 6 {:id 7 :name "Jim Egbot"})
(modify-person 7 {:id 6 :name "John Egbert"})