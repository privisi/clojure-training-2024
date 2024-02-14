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
(def test-person-params {:name "Test McTesterson"
                         :email "foo@bar.com"
                         :phone "99999999"})
(def test-org-params {:name "Test McTesterson"
                         :email "foo@bar.com"
                         :phone "99999999"})


(defn fetch-data
  "Generic request for data. Applies the hardcoded API token."
  ([url] ; for getting all info from the request
   (future (-> (client/get
                (str url)
                {:as :json
                 :query-params {:api_token api-key}})
               (:body))))
  ([url id-num] ; Pipedrive doesn't do server-side filtering with query params, rerouting...
    ;; [url & params]
    ;; (let [params-map (apply hash-map params)] (
     (let [url-addition (str "/" id-num "/")]
       (future (-> (client/get
                    (str url url-addition) ; 
                    {:as :json
                  ;; :query-params (merge {:api_token api-key} params-map)}) 
                     :query-params {:api_token api-key}})
                   (:body))))))


;; (defn fetch-person
;;   "Creates a future request for a person's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data persons-url key value))
;; (defn fetch-org
;;   "Creates a future request for an organization's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data organizations-url key value))
;; (defn fetch-lead
;;   "Creates a future request for a lead's data that matches supplied key and value."
;;   [key value]
;;   (fetch-data leads-url key value))

(defn fetch-person-by-id
  "Creates a future request for a person's data that matches supplied key and value."
  [id-num]
  (fetch-data persons-url id-num))
(defn fetch-org-by-id
  "Creates a future request for an organization's data that matches supplied key and value."
  [id-num]
  (fetch-data organizations-url id-num))
(defn fetch-lead-by-id
  "Creates a future request for a lead's data that matches supplied key and value."
  [id-num]
  (fetch-data leads-url id-num))

(defn fetch-all-persons
  "Creates a future request for all people in the database."
  []
  (fetch-data persons-url))
(defn fetch-all-orgs
  "Creates a future request for all organizations in the database."
  []
  (fetch-data organizations-url))
(defn fetch-all-leads
  "Creates a future request for all leads in the database."
  []
  (fetch-data leads-url))


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
  "Creates a new organization in the database with the supplied map."
  [params-map]
  (post-data organizations-url params-map))



(defn output-entity-data
  "Gets entity data using the API key and outputs a map with their data."
  [url id-num]
  (let [entity-future (fetch-data url id-num)]
    (-> (deref entity-future 3000 (str "Search for ID " id-num " timed out."))
        :data)))

(defn output-person-data 
  "Gets a person using the API key and outputs a map with their data."
  [id-num]
  (output-entity-data persons-url id-num))
(defn output-org-data
  "Gets an organization using the API key and outputs a map with their data."
  [id-num]
 (output-entity-data organizations-url id-num))
(defn output-lead-data
  "Gets a person using the API key and outputs a map with their data."
  [id-num]
  (output-entity-data leads-url id-num))


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
  (map strip-person-data (get (deref (fetch-all-persons)) :data)))

(defn strip-org-data
  "Reduces the full map JSON to just name, address, postal code, country, and related deals."
  [org] 
  {:id                   (:id                 org)
   :name                 (:name               org)
   :address              (:address            org)
   :address_postal_code (:address_postal_code org)
   :address_country      (:address_country    org)
   :deals                (:closed_deals_count org)})
(defn strip-all-organizations-data []
  (map strip-org-data (get (deref (fetch-all-orgs)) :data)))

(defn strip-lead-data
  "Reduces the full map JSON to just name, phone, email, company, and their deal count."
  [lead]
  (let [deal-amount (get-in lead [:value :amount])
        deal-currency (get-in lead [:value :currency])]
  {:organization_id      (:organization_id      lead)
   :title                (:title               lead)
   :amount               deal-amount
   :currency             deal-currency
   :expected_close_date  (:expected_close_date lead)
   :contact              (get (deref (fetch-person-by-id (get lead :person_id))) :name)}))
(defn strip-all-leads-data []
  (map strip-lead-data (get (deref (fetch-all-leads)) :data)))



(deref (fetch-all-persons))
(add-person test-person-params)
(output-person-data 2)
(strip-person-data (output-person-data 3))
(output-org-data 1)
(strip-org-data (output-org-data 2))
(strip-all-persons-data)
(strip-all-organizations-data)
(strip-all-leads-data)