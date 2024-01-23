(ns clojure-training-2024.lesson04)

(def some-map
  {:foo 77
   :bar 99
   \a   "A"
   {:hello "world"} "saying"
   "strings" :the-keyword})

(keys some-map) ; (:foo :bar \a {:hello "world"} "strings")
(vals some-map) ; (77 99 "A" "saying" :the-keyword)

(some-map {:hello "world"}) ; "saying"
(some-map \a) ; "A"
(some-map \b 123) ; 123

(:foo some-map) ; 77
;; you can only put the keyword first if trying to short-form get from a map
;; ("strings" some-map) ; wont compile
;; If you do the other way, using map as a function, you can put anything
(some-map "strings") ; :the-keyword

:2 :!

;; - Another misunderstanding about booleans:
;;   There is no virtue in returning true explicitly
;;   instead of "any truthy value".

(comment
  (if  (do-some-work)
    true
    false)

  (def some-var 1)

  (cond
    some-var            "we run "
    (= some-var true)   "do-some"))


;;;; Atoms


(def bank-account
  "An IDENTITY, which corresponds to a representation of the
   current VALUE of Bob's bank account.

   Values are immutable, but identities can take on a succession
   of differing values as time progresses in our system."
  (atom {:customer   "Bob"
         :balance    50
         :updated-at (java.util.Date.)}))

bank-account ; #atom[{:customer "Bob" ...} 0x5a8f5fe2]
(deref bank-account) ; {:customer "Bob", :balance 50, :updated-at #inst "2024-01-23T11:54:35.721-00:00"}
@bank-account ; short form for deref

(defn deposit! [account-atom amount]
  ;; The way we change the VALUE associated with the IDENTITY _account_
  ;; is by calling SWAP!
  ;; The bang (!) is mnemonic for "performs a mutable operation"
  ;; (in the way that (?) is mnemonic for "predicate".
  ;; CAS
  (swap! account-atom update :balance + amount))

(deposit! bank-account 73) ; swap returns the updated value
;; -> {:customer "Bob", :balance 123, :updated-at #inst "2024-01-23T11:54:35.721-00:00"}

@bank-account ; -> {:customer "Bob", :balance 123, :updated-at #inst "2024-01-23T11:54:35.721-00:00"}

(def remember-the-past-when-I-had-123-dollars
  (deref bank-account))

(:balance remember-the-past-when-I-had-123-dollars) ; 123
(deposit! bank-account 50)
(:balance @bank-account) ; 173

(defn the-past-or-the-future? [account-atom]
  (:balance @account-atom))

(the-past-or-the-future? bank-account) ; 173
(deposit! bank-account 50)
(the-past-or-the-future? bank-account) ; 223

(defn long-complex-calculation [bank-account]
  (let [initial-value @bank-account]
    ;; do something that takes an hour...
    ))

;; The ONLY WAY of modifying the value associated with an identity
;; is via swap!, and observers (other threads doing (deref bank-account)
;; can only ever see a value which is before, or after, the function requested
;; by swap! completes.  No in-between.  We call this an ATOMIC swap.
;; (atomic --- from the greek: indivisible).


;;;;
;; What if we want to modify more than one field at a time?
;; We could try:

(defn deposit!
  [account amount]
  (swap! account update :balance + amount)             ; Don't do this!!!
  ;;   deposit!
  (swap! account assoc :updated-at (java.util.Date.)))

(defn deposit!
  [account amount]
  (swap! account
         #(-> %
              (update :balance + amount)
              (assoc :updated-at (java.util.Date.)))))


;; Now observers of bank-account either see the old or the new value, never
;; an inconsistent balance and transaction time.


@bank-account

(deposit! bank-account 73)
@bank-account


;;;; Refs

;; What if we have to maintain _multiple_ identities in a consistent way?
;; e.g. what if we have a set of "Gold members", and you become a gold member
;; if your account has more than $1,000,000 ?

(def ^{:doc "The set of names of those most exalted of bank account holders."}
  gold-members
  (atom #{}))

;; We could write:
(defn deposit! [account amount]
  (swap! account
         #(-> %
              (update :balance + amount)
              (assoc :updated-at (java.util.Date.))))
  (when (> 1000000 (:balance @account))
    ;; What happens if another thread does a big withdrawal right here?
    (swap! gold-members conj (:customer @account))))

;; This is called a RACE CONDITION.
;; Databases handle this problem by a mechanism called a TRANSACTION,
;; where all changes are committed, or none are committed, and any
;; thread in a transaction sees (and keeps seeing) consistent values
;; until the end of the transaction, no matter what other threads are doing.

;; Clojure uses the same mechanism, with something called REFS.
;; STM  (software transactional memory)
;; We write, instead:

(def bank-account
  "An IDENTITY, which corresponds to a representation of the
   current VALUE of Bob's bank account.

   Values are immutable, but identities can take on a succession
   of differing values as time progresses in our system."
  (ref {:customer "Bob"
        :balance  50
        :updated-at (java.util.Date.)}))

(def ^{:doc "The set of names of those most exalted of bank account holders."}
  gold-members
  (ref #{}))

(defn deposit! [account amount]
  (dosync
   (alter account
          #(-> %
               (update :balance + amount)
               (assoc :updated-at (java.util.Date.))))
   #_
   (Thread/sleep 5000)
   (when (> (:balance @account) 1000000)
     ;; What happens if another thread does a big withdrawal right here?
     ;; One of 2 things:
     ;; They block (we win, and continue), OR
     ;; clojure throws an exception, and RETRIES the entire
     ;; DOSYNC block, until we can act on consistent values.
     (alter gold-members conj (:customer @account)))))

(deposit! bank-account 1)
@gold-members
@bank-account

;;;; Agents

;; Allow uncoordinated, atomic change

(def james-bond (agent {:kills 0 :victims #{}}))

(defn murder [spy victim]
  (-> spy
      (update :kills inc)
      (update :victims conj victim)))

; or alter or swap!
; 1st arg is always the "old" value

(send james-bond murder "Dr. No")
;; i don't get the return value
;; #agent[{:status :ready, :val {:kills 1, :victims #{"Dr. No"}}} 0x3e357795]
;; swap! and alter are returning the dereferenced state at that point of modification

(deref james-bond) ; {:kills 1, :victims #{"Dr. No"}}

(send james-bond murder "Goldfinger")

;; We'll see later, with threads, how this can be used.
;; One interesting property: SENDs are held off until the
;; end of a txn, so

;; Also send-off.


(comment
  (dosync
   (alter foo hack-hack)
   (swap! blah do-some-work)
   (alter bar frobnicate)
  ;; This will be safe!  We will get only 1 log message.
   (send log-agent log "I managed to alter both foo and bar.")))

;; Refs are for Coordinated Synchronous access to "Many Identities" .
;; Atoms are for Uncoordinated synchronous access to a single Identity.
;; Agents are for Uncoordinated asynchronous access to a single Identity.

;; Coordinated access is used when two Identities needs to change together,
;; the classic example is moving money from one bank account to another,
;; it needs to either move completely or not at all.

;; Uncoordinated access is used when only one Identity needs to update,
;; this is a very common case.

;; Synchronous access is used when the call is expected to wait until all
;; Identities have settled before continuing.



;;;;  Threading constructs.

;;; Futures

;; - starts a new thread
;; - doesn't block code in which future is called

(defn do-some-work []
  (Thread/sleep 6000) ; Mimic some CPU intensive work.
  (println "Word done on: " (.getName (Thread/currentThread)))
  (rand-int 100))

#_
(do-some-work)

(defn some-busy-fn []
  (println "I do some stuff")
  (future (do-some-work))
  ;; Didn't block.
  (println "I did some stuff"))

#_
(some-busy-fn)

(defn another-busy-fn []
  (let [result (future (do-some-work))]
    (println "I do some stuff")
    ;; Now I need the result:
    (println "Our work yielded: " @result)
    (println "All done.")))

(another-busy-fn)

;; Note -- @foo is just a macro which expands into (deref foo)
;; But deref takes some interesting extra arguments:
(defn an-impatient-fn []
  (let [result (future (do (do-some-work)
                           (println "I did the work anyway")))]
   (println "I do some stuff")
   ;; Now I need the result:
   (println "Our work yielded: " (deref result 1000 :too-late))
   (println "All done.")))

(an-impatient-fn)

;;; A note on dynamic vars
(def ^:dynamic *foo* 42)

(def d2
  (binding [*foo* 69]
    (future
      (Thread/sleep 5000)
      (println "In the future:")
      (println "We have " *foo*))))
#_
d2

;;;; Delays

;; Sort of like futures, but might never get executed

;; The example in the book is very neat, make sure you understand it.

;; Another common use is to do something like:

(def  some-unknown-number (delay (rand-int 1000)))

@some-unknown-number

(comment
  (def some-resource
    ;; We can't get the value of some-resource when we load the file,
    ;; before the program has started.  Maybe it needs to wait until
    ;; main has connected to a database or something.
    (delay (expensive-operation-to-acquire-resource-at-runtime)))


  (defn hairy-function []
    ;; .... code code
    ;; Force evaluation of some-resource; only runs first time.
    (foobar @some-resource)))


;;;; Promises

;; Can act sort of like a synchronization point.

;; Easy to get your knickers in a twist with promises, so be careful!

;; We will see better ways of doing this sort of thing later with core.async CHANNELS.

(def p (promise))


(future
  (println "I'm waiting for it!")
  (println "I got: " @p))

(deliver p "Rhythm")

(deliver p "nuthin")

;;;; Other mechanisms:

;; Plain java thread:


(def java-thread
  (java.lang.Thread. #(println "Hello from " (java.lang.Thread/currentThread))))

(.start java-thread)

;; Note -- above! won't convey binding of specials!

;; Also, clojure.core.async, but we'll cover that later.

(defn savvy-saver [bank-account]
  (future
    (dotimes [i 10]
      (deposit! bank-account (rand-int 100))
      (println "I made a deposit! I now have" (:balance @bank-account))
      (Thread/sleep (rand-int 2000)))))


(defn observer []
  (future
    (dotimes [i 10]
      (println "The account at time " i " said: " @bank-account)
      (Thread/sleep 5000))))

(comment
  (observer)
  (savvy-saver bank-account))



;;;;
(def money (ref 1000))

(defn add-money [money]
  (future
    (dosync
     (dotimes [i 10]
       (println "Adding money: " @money)
       (Thread/sleep 1000)
       (alter money inc)))))

(defn withdraw-money [money]
  (future
    (dosync
     (dotimes [i 10]
       (println "Withdrawing money: " @money)
       (Thread/sleep 1000)
       (alter money dec)))))

(comment
  (add-money money)
  (withdraw-money money))

;;;; Debugging
;;   https://calva.io/debugger/

;; Instrument a function to be debugged
;; -> ctrl + alt + c, i
(defn debug-test [a b]
  (-> a
      (+ b)
      (+ b)
      (+ b)
      (+ b)
      (+ b)))

#_
(debug-test 1 2)

;; Set a break point at a certain area
(defn debug-test [a b]
  (-> a
      (+ b)
      #break
       (+ b)
      (+ b)
      #break
       (+ b)
      (+ b)))

#_(debug-test 1 2)

;; Conditional breaks

(defn print-nums [n]
  (dotimes [i n]
    #break ^{:break/when (= i 7)} ;; This breakpoint will only be hit when i equals 7
     (prn i)))

(print-nums 10)

;; Look into https://calva.io/debugger/