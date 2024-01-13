(ns clojure-training-2024.lesson01)

; this is also a comment
; in java -> //
; in clojure -> any amount of ;

; this is usually used next to code
(+ 1 1) ; add together
;; this is used in-line
;; this does adding
(+ 1 1)
;;; This is a section header
;;; Section for adding

;;;; This is also a section header but typically for the whole file

(comment (this is commented out call))

#_(this comments out a chunck of code going
        from start to end of bracket section
        (+ 1 2
           (* 3 4))) ; up to the end of the bracket

;; (operator operand1 operand2 ...)
;; (function 1 2 3)
;; (function) - no args is fine too

(+ 1 2)

(+ 1
   (+ 2
      (+ 3
         (+ 4 5))))

;; verbose, you can do this way:

(+ 1 2 3 4 5)

;; operators are almost, but not always, FUNCTIONS.

;;;; Fundamental Types

"Hello"

7

7.0

7/3 ; exact ratio

(* 7/3 3)

(* (/ 7.0 3.0)
   3.0)

7N

java.lang.Long/MAX_VALUE

#_
(+ 9223372036854775807 1)

(+ 9223372036854775807N 1)

(+ 1 1.3)

;; The REPL (Read Eval Print Loop)
(comment
  (loop
   (print
    (eval
     (read)))))

:foo-bar ; I am a keyword

(comment
  some-symbol) ; I am a symbol

; symbols need to be declared as something or they wont exist

[1 2 3] ; I am a vector
(vector 1 2 3)
[1 :banner "split"]

;; Sets
#{1 2 3}

(= #{1 2 3} #{3 2 1})

(conj #{1 2 3} 4)

(into #{} (range 99))

;; conjs at the end
(conj [1 2 3] 99)

(conj {:foo 99} {:bar 99})
(conj {:foo 99} [:vector 99])
(conj '(1 2 3) 0)

{:liquid "Milk"
 :colour "White"
 :price  299}

{:plus  +
 :minus -}

{+ "this is plus"}

{"foo" :bar} ; we can put anything for a key / value

{{:foo :bar} :baz} ; even a map in a map (the inner map is the key)

;;; VALUES

;;; using DEF for VARS

(def foo 7)

foo ; symbol foo

(def grocery-entry {:liquid "Milk"
                    :colour "White"
                    :price  299})

;; ASSOC

;; Clojure defines states, you can modify the value into a different state,
;; But you can never change existing states.

;; Create a new state
(assoc grocery-entry :on-special? true)
;; => {:liquid "Milk", :colour "White", :price 299, :on-special? true}
;; Modified state, the original grocery-entry is never changes
grocery-entry
;; => {:liquid "Milk", :colour "White", :price 299}

(dissoc grocery-entry :price)
;; => {:liquid "Milk", :colour "White"}

(update grocery-entry :price inc)
;; => {:liquid "Milk", :colour "White", :price 300} - price has inc'd

(update grocery-entry :price + 50)
;; => {:liquid "Milk", :colour "White", :price 349} - price has (+ price 50)

(defn put-on-special [entry discount]
  (dissoc
   (update (assoc entry :on-special? true)
           :price * discount)
   :liquid))

(put-on-special grocery-entry 0.75)
;; => {:colour "White", :price 224.25, :on-special? true}

(defn put-on-special [entry discount]
  (-> entry
      (assoc :on-special? true)
      (update :price * discount)
      (dissoc :liquid)))

(put-on-special grocery-entry 0.75)

;;;; FUNCTIONAL PROGRAMMING

;; this is a function

(fn [x] (* x x))

;; you can define them and give them names

(def square (fn [x] (* x x)))

;; This was so common that defn was created as a short cut.

(defn square [x] (* x x))

;; These are called first class functions.
;; Functional programming is about usng functions as first class
;; objects in your program; that is both as arguments AND return
;; values to functions

;; higher order functions

(map square [1 2 3 4 5])

;; map is called a higher order function, because one or more
;; of it's arguments are themselves functions.

;; Clojure is full of higher order functions
;; can you name some?

(filter odd? [1 2 3 4 5])
(remove odd? [1 2 3 4 5])

(defn f [x] (+ 1 x))

(defn g [x] (* 2 x))


;; Comp returns a function which is the composite of multiple functions
;; It will apply the functions from the right first (g -> f)
(def h (comp f g))

(map h [1 2 3])

;; complement returns a function that is the opposite of the function provided
(def my-even (complement odd?))

(filter my-even [1 2 3 4 5]) ; behaves like even

(filter even? [1 2 3 4 5])

(clojure.repl/doc filter)


;; special form: LET

(let [x 7
      y 3]
  ;; In here x and y exist
  (+ x y))

(defn add-3 [number]
  (let [to-add 3]
    (+ number to-add)))

(add-3 10)

;; Generalize this:

;; This returns a function
(defn make-n-adder [n]
  (let [x n]
    (fn [y] (+ x y))))

;; returns a function
(make-n-adder 59)

;; use the function that it returned

((make-n-adder 59) 10)

;; utility for sequences

(first [1 2 3])
(rest [1 2 3])
(last [1 2 3])

(first [])
(rest [])

(seq [1 2 3])
(seq [])

(if []
  "true response"
  "false response")

(if (seq [])
  "true response"
  "false response")

;; Basoc structure of the LOOP macro
(loop [x [1 2 3]]
  (println x)
  (if (seq (rest x))
    (recur (rest x))))

(loop [nums-to-add [1 2 3]
       total       0]
  (if (seq nums-to-add)
    (recur (rest nums-to-add) (+ total (first nums-to-add)))
    total))