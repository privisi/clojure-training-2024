(ns clojure-training-2024.lesson02)

(defn full-function [x y z]
  (+ x y (* z z)))
;; =
(fn [x y z]
  (+ x y (* z z)))
;; =
;; anonymous function with multiple args need to hard a number after the % to indicate
;; which arg it is refering to %1 %2 = [%1 %2]
#(+ %1 %2 (* %3 %3))

;; anonymous function with only 1 arg does not need a number after the %
;; anon-fn for adding 5
#(+ 5 %)

(def my-foo (fn [x y] (+ x y)))
;; defn was made as a macro to turn into that
(macroexpand-1 '(defn my-foo [x y]
                  (+ x y)))
;; => (def my-foo (clojure.core/fn ([x y] (+ x y))))
(fn my-foo [x y] (* x y))
(def my-foo #(+ %1 %2))
;; all of these do the same thing ^

(let [y 5]
  (map #(+ %1 %2 y)
       [1 2 3]
       [4 5 6]))
;; => (10 12 14)

(let [x 3
      y 7]
  (+ x y))

;; what if we write
(let [x 4]
  (let [x 7]
    (str "X is now " x))
  (str "X is finally " x))
;; "X is finally 4"

(if true
  "got true")

(when true
  "this is true")
;; "this is true"

(when false
  "this is true")
;; nil
;; also see if-not when-not etc.


;; Closure (not clojure)
;; we say that the returned function CLOSES over the variable x
(def blah
  (let [x 3]
    (fn [y]
      (+ x y))))

(blah 6) ; => 9


;;;; Dynamic scope

(def ^:dynamic *bigvar* 42)
(def regular 42)

(defn blah []
  *bigvar*)

(defn blah-regular []
  regular)

(blah) ;42
(blah-regular) ;42

(binding [*bigvar* 9999]
  (blah)) ; 9999

(let [regular 9999]
  (blah-regular)) ; 42

;;; Dynamic bindings can act as thread-local "global" variables.
;;; But they are frowned upon in clojure, as they break REFERENTIAL TRANSPARENCY.

;; referential transparency: the property that a pure function, given
;; some arguments, will ALWAYS return the same value!


;;;; Destructuring

(let [x [1 2 3]]
  (first x)
  (rest x)
  (second x))

(let [[a b c] [1 2 3]]
  {:a a
   :b b
   :c c}) ; {:a 1, :b 2, :c 3}

(let [[a & more] [1 2 3]]
  {:a    a
   :more more}) ; {:a 1, :more (2 3)}

;;; Destructuring maps --- memorize this!!

(def grocery-entry
  {:liquid "Milk"
   :colour "white"
   :price  299})

[(:liquid grocery-entry) (:colour grocery-entry)] ; ["Milk" "white"]

(let [{liq :liquid col :colour} grocery-entry]
  [liq col]) ; ["Milk" "white"]

(let [{liq :liquid col :color u :unknown} grocery-entry] ; Notice the typo!!
  ;; Source of many a hard to track bug.  The perils of dynamic typing.
  [liq col u]) ; ["Milk" nil nil]


;;; Another, very common way of destructuring a map:
grocery-entry

(let [colour (get grocery-entry :liquid)
      liquid (get grocery-entry :colour)]
  ;; scope.
  [liquid colour]) ; ["white" "Milk"]

(let [[& more] {:status 200 :method "GET" :query-string "foo=7&bar=9"}]
  ;; scope.
  more) ; ([:status 200] [:method "GET"] [:query-string "foo=7&bar=9"])

;; Where can you find out all this crazy stuff?
;; You must (eventually) read the reference, all the way through,
;; at least once.  Destructuring is covered here:
;; https://clojure.org/reference/special_forms#binding-forms

(let [[first-name last-name :as full-name] ["Stephen" "Hawking"]]
  full-name) ; ["Stephen" "Hawking"]

(let [{liq :liquid :as m} grocery-entry]
  (str "Liquid of entry: " m " is - " liq))
;; "Liquid of entry: {:liquid \"Milk\", :colour \"white\", :price 299} is - Milk"

(let [{:keys [liquid colour price] :as m} grocery-entry]
  [liquid colour price m])
;; ["Milk" "white" 299 {:liquid "Milk", :colour "white", :price 299}]

(defn what-liquid? [grocery-entry]
  (:liquid grocery-entry))

(what-liquid? grocery-entry) ; "Milk"

(defn what-liquid? [{:keys [liquid]}]
  liquid)

(what-liquid? grocery-entry) ; "Milk"

;;; Anywhere that clojure lets you bind variables, it lets you destructure,
;;; which is very convenient.

(loop [[num & rest-nums] [1 2 3 4 5]
       total             0]
  (if num
    (recur rest-nums (+ total num))
    total)) ; 15

;; Last time we noticed:
(rest [1 2 3]) ; not a vector ?!

;; Basic structure of the LOOP macro.
(loop [x [1 2 3]]
  (println [x (class x)])
  (if (seq (rest x))
    (recur (rest x))))

;; [[1 2 3] clojure.lang.PersistentVector]
;; [(2 3) clojure.lang.PersistentVector$ChunkedSeq]
;; [(3) clojure.lang.PersistentVector$ChunkedSeq]

;; seq
;; -> vector
;; -> list
;;    etc.


;; clojure tries to encourage LAZINESS.  e.g.

(def all-integers (range)) ; safe to evaluate!
all-integers

;; A lazy sequence is a unrealized sequence (instructions to do things,
;; but we haven't done them yet because nothing needs it)

;; These needs 10, so we will realize enough to fulfill this.
(take 10 all-integers)

(->> all-integers ; The "thread last" macro
     (drop 9999999)
     (take 10))

;; ->  threads into the FIRST position
;; ->> threads into the LAST position

(-> grocery-entry
    (assoc :on-special? true)
    ;;    ^ here is where grocery-entry gets spliced in
    (dissoc :liquid)
    ;;    ^ here is where result of above computation gets spliced in
    )

(->> all-integers ; The "thread last" macro
     (drop 77)
     ;;      ^ here is where all-integers gets spliced
     (take 10)
     ;;      ^ here is where result of the drop 10 gets spliced
     (map inc))

;; Functions which create and return lazy sequences:

repeat repeatedly
take
drop
iterate

(take 5 (repeat :foo))

(zipmap [:a :b :c]
        (range))

(class (range 5 9)) ; Both just SEQS.
(class (range))

(let [x (map println [1 2 3 4 5])
      y (println "YAAAAAA")]
  x)

(let [x (doall (map println [6 7 8 9]))])

;; DONT RUN THIS
#_(doall (map println (range)))


;;;;  * reduce, functional idioms

;; Remember this problem?  Histogram of letters?

(loop [[ch & str] "hello"
       hist {}]
  (if ch
    ;; more characters?
    (recur str
           ;; Modify the map each time through the loop
           (assoc hist
                  ch
                  (+ 1 (get hist ch 0))))
    hist))

#_(update {} :i-dont-exist inc) ;; errors

(update {} :i-dont-exist (fnil inc 0))

;; fnil defaults the function arg if it is nil
((fnil (fn [x]
         (str "We got given: " x))
       :some-default)
 nil) ; "We got given: :some-default"


(reduce + 0 [1 2 3 4 5]) ; 15
(reduce + 21 [1 2 3 4 5]); 36

(defn my-plus [total num-to-add]
  (+ total num-to-add))

(reduce my-plus 0 [1 2 3 4 5]) ; 15

;; what if we don't provide a default value?
(reduce my-plus [1 2 3 4 5]) ; 15
;; it's okay, it actually takes the first value as the default
;; and begins on the second value in the collection
;; equivalent to => (reduce my-plus 1 [2 3 4 5])

(def my-number 99)

(defn- add-with-another-number [another-number total number]
  (+ total number another-number))

(reduce (partial add-with-another-number my-number) [1 2 3 4 5]) ; 411
;; this is the same thing:
(reduce #(add-with-another-number my-number %1 %2) [1 2 3 4 5]) ; 411
;; this is the same thing:
(reduce (fn [total number]
          (add-with-another-number my-number total number))
        [1 2 3 4 5]) ; 411

(reductions my-plus [1 2 3 4 5]) ; (1 3 6 10 15)
;; reductions returns to you a map, of each value at each loop / step
;; (1   => default val
;;  3   => 1 + 2
;;  6   => 3 + 3
;;  10  => 6 + 4
;;  15) => 10 + 5

;; Going back to histogram
(reduce (fn [hist letter]
          (update hist letter (fnil inc 0)))
        {}
        "hello") ; {\h 1, \e 1, \l 2, \o 1}

(frequencies "hello")

