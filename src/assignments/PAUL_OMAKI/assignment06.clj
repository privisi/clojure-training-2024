(ns assignments.PAUL-OMAKI.assignment06)



(def vending-machine "Vending machine base state.
                      
                      `:state :start`
                      `:coins []`"
  {:state :start :coins []})

(def price-of-soda 220)

(defn coin-value
  "The sum value of all the coins currently in the machine."
  [machine]
  (reduce + (:coins machine)))

;; split out for ease of reading code
(defn dispense-if-enough-money
  "Checks the state of the vending machine to see if there is enough to purchase the soda.
   If there is, dispenses soda and refunds whatever is left in the machine."
  ([machine]
   (dispense-if-enough-money machine :cola) )
  ([machine drink]
   (if (< (coin-value machine) price-of-soda)
     (do 
       (println "Not enough money.")
       machine)
     (if (< 0 (- (coin-value machine) price-of-soda))
       (do                               ; We have enough money! Too much of it, in fact!
         (println "Disbursing delicious soda: " drink)
         (println "Refunding " (- (coin-value machine) price-of-soda) " cents.")
         vending-machine)
       (do                               ; We have just enough money! No need to refund!
         (println "Disbursing delicious soda: " drink)
         vending-machine)))))



(defmulti handle
  "Handler for the vending machine. 
   
   Events:

   `coin`    Coin inserted into machine.

   `button`  Button to vend soda pressed.

   `refund`  Customer pressed refund lever.

   States:

   `start`              No input, no coins.

   `ready` Money has been inserted or button pressed, machine awaiting button input."
  (fn [machine event]
                   [(:state machine) (:type event)]))

(defmethod handle [:start :coin] [machine event]
  (println "Getting a coin in the start state: " (:coin-value event) " cents.")
  (-> machine
      (assoc :state :ready)
      (update :coins conj (:coin-value event))))

(defmethod handle [:start :button] [machine event]
  (println "Button pressed: " (:button-value event))
  (println "Please insert money or select payment type.")
  (assoc machine :state :ready))

(defmethod handle [:start :refund] [machine event]
  (println event " activated.")
  (if (seq (:coins machine))
    (println "Refunding " (:coins machine)) ; if there are somehow coins in in start state
    (println "Nothing to refund."))
  vending-machine)

(defmethod handle [:ready :refund] [machine event]
  (println "Ready state, but " event " activated.")
  (println "Refunding " (:coins machine))
  vending-machine)

(defmethod handle [:ready :coin] [machine event]
  (println "Getting a coin in the ready state: " (:coin-value event) " cents added.")
           (let [machine (-> machine
                             (assoc :state :ready)
                             (update :coins conj (:coin-value event)))]
             machine))

(defmethod handle [:ready :button] [machine event]
  (println "Button pressed: " (:button-value event))
  (let [machine  machine
        drink    (:button-value event)]
    (dispense-if-enough-money machine drink)))



;; Buyer is very impatient
(-> vending-machine 
    (handle {:type :button :button-value :cola})
    (handle {:type :coin :coin-value 20})
    (handle {:type :button :button-value :cola})
    (handle {:type :button :button-value :cola})
    (handle {:type :coin :coin-value 200})
    (handle {:type :button :button-value :cola})
    (handle {:type :refund})
    (handle {:type :refund})
    (handle {:type :refund})
    (handle {:type :refund}))


;;; Assignment: Extend this machine so it can make proper change
;;; if user pays too much.

;; A common pattern with state machines:

(let [events [{:type :coin :coin-value 20}
              {:type :coin :coin-value 20}
              {:type :button :button-value :cola}
              {:type :refund}
              {:type :button :button-value :cola}
              {:type :coin :coin-value 200}
              {:type :coin :coin-value 200}
              {:type :button :button-value :cola}
              {:type :coin :coin-value 200}
              {:type :coin :coin-value 200}
              {:type :coin :coin-value 10}
              {:type :coin :coin-value 2000}
              {:type :coin :coin-value 200}
              {:type :button :button-value :cola}
              {:type :button :button-value :cola}
              {:type :button :button-value :cola}
              {:type :button :button-value :cola}]]
 
  
  (reduce handle
          vending-machine
          events))



;; 4clojure problem #46

(((fn [f] (fn [x y] (f y x))) take) [1 2 3 4 5] 3)



;; 4clojure problem #49

(defn cut-at-point [cut-point list] (conj [] (vec (take cut-point list)) (vec (drop cut-point list))))

(cut-at-point 3 [1 2 3 4 5 6])

(cut-at-point 1 [:a :b :c :d])

(cut-at-point 2 [[1 2] [3 4] [5 6]]) 
