(ns clojure-training-2024.lesson08
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.java.jdbc :as jdbc]
   [clojure.core.async :as a :refer [>! <! >!! <!! go chan buffer close! thread
                                     alts! alts!! timeout]]
   [medley.core :as m]))

(def echo-chan (chan))
#_
(go (println (<! echo-chan)))
#_
(>!! echo-chan "ketchup")


(def echo-buffer (chan 2))

#_
(>!! echo-buffer "ketchup")

(def hi-chan (chan))

#_
(doseq [n (range 100)]
  (println n)
  (go (>! hi-chan (str "hi " n))))


(thread (println (<!! echo-chan)))

#_
(>!! echo-chan "mustard")

(let [t (thread "chili")]
  (<!! t))

(defn cannon []
  (let [ammo-compartment (chan)
        cannon-barrel    (chan)]
    (go (<! ammo-compartment)
        (>! cannon-barrel "Boom"))
    [ammo-compartment cannon-barrel]))

(let [[in out] (cannon)]
  (>!! in "cannon ball")
  (<!! out))

(defn railgun [terawatts]
  (let [in  (chan)
        out (chan)]
    (go (loop [charges terawatts]
          (if (pos? charges)
            ;; Attempt to fire it
            (let [ammo (<! in)]
              (if (= ammo "coin")
                (do (>! out "coin railgun")
                    (recur (dec charges)))
                (do (>! out "It fell out without doing much")
                    (recur (dec charges)))))
            ;; out of charges so close both in and out channels
            (do (close! in)
                (close! out)))))
    [in out]))

(let [[in out] (railgun 3)]
  (>!! in "pocket lint")
  (println (<!! out))

  (>!! in "coin")
  (println (<!! out))

  (>!! in "coin")
  (println (<!! out))

  (>!! in "coin")
  (println (<!! out))

  (>!! in "coin")
  (println (<!! out)))


;; pipeline
(let [string-modifier (chan)
      capitalizer     (chan)
      reverser        (chan)]
  (go (>! capitalizer (clojure.string/upper-case (<! string-modifier))))
  (go (>! reverser (clojure.string/reverse (<! capitalizer))))
  (go (println (<! reverser)))
  (>!! string-modifier "redrum"))


(defn campfire-cook [food grate]
  (go (Thread/sleep (rand 100))
      (>! grate food)))

(let [grate1 (chan)
      grate2 (chan)
      grate3 (chan)]
  (campfire-cook "frog" grate1)
  (campfire-cook "beef" grate2)
  (campfire-cook "broccoli" grate3)
  (let [[food channel] (alts!! [grate1 grate2 grate3])]
    (println "This got cooked first: " food)))

(let [c1 (chan)
      c2 (chan)]
  (go (<! c2))
  (let [[value channel] (alts!! [c1 [c2 "fdsdf"]])]
    (println value)
    (= channel c2)))

;;;; queues

(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "\n=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (rand-int 99999999)))

(defn snag-quotes [filename num-quotes]
  (let [c (chan)]
    (go (while true
          (append-to-file filename (<! c))))
    (dotimes [n num-quotes]
      (go (>! c (random-quote))))))

#_(snag-quotes "asyncspit.txt" 5)

;;;; Process Pipelines

(defn upper-caser [in]
  (let [out (chan)]
    (go (while true
          (>! out (clojure.string/upper-case (<! in)))))
    out))

(defn reverser [in]
  (let [out (chan)]
    (go (while true
          (>! out (clojure.string/reverse (<! in)))))
    out))

(defn printer [in]
  (go (while true
        (println (<! in)))))

(defonce in-chan (chan))
(defonce upper-caser-out (upper-caser in-chan))
(defonce reverser-out (reverser upper-caser-out))

(printer reverser-out)

(>!! in-chan "redrum")
(>!! in-chan "repaid")



;;;; POSTGRES


(def db-conn
  {:dbtype   "postgresql"
   :host     "localhost"
   :dbname   "postgres"
   :user     "postgres"
   :password "postgres"})

;; Check if it's working
(jdbc/query db-conn ["SELECT 1"])

(jdbc/query db-conn ["SELECT ?" 4])

(jdbc/query db-conn
            ["SELECT 1 AS our_num"]
            {:row-fn #(assoc % :hello "world")})

;; CREATE TABLE students (
;;     student_id SERIAL PRIMARY KEY,
;;     first_name VARCHAR(50),
;;     last_name VARCHAR(50),
;;     age INT,
;;     grade CHAR(1)
;; )

(jdbc/execute! db-conn
               ["CREATE TABLE students (
                  student_id SERIAL PRIMARY KEY,
                  first_name VARCHAR(50),
                  last_name VARCHAR(50),
                  age INT,
                  grade CHAR(1) DEFAULT 'f')"])


;; INSERT INTO students (first_name, last_name, age, grade)
;; VALUES
;;     ('John', 'Doe', 20, 'A'),
;;     ('Jane', 'Smith', 22, 'B'),
;;     ('Bob', 'Johnson', 19, 'C')

(jdbc/execute! db-conn
               ["INSERT INTO students (first_name, last_name, age, grade)
                               VALUES ('John', 'Doe', 20, 'A'),
                                      ('Jane', 'Smith', 22, 'B'),
                                      ('Bob', 'Johnson', 19, 'C')"])

(jdbc/execute! db-conn
               ["INSERT INTO students (first_name, last_name, age)
                               VALUES ('Misaka', 'Kyaru', 16)"])

;; SELECT * FROM students

(defn create-full-name [{:keys [first-name last-name]}]
  (str first-name " " last-name))

(jdbc/query db-conn ["SELECT * FROM students"])
(->> (jdbc/query db-conn ["SELECT first_name, last_name FROM students"]
                 {:row-fn #(m/map-keys csk/->kebab-case-keyword %)})
     (map create-full-name))


;; UPDATE students
;; SET age = 21
;; WHERE student_id = 1

(jdbc/execute! db-conn
               ["UPDATE students
                    SET age = 999
                  WHERE student_id = 1"])

;; DELETE FROM students
;; WHERE student_id = 3

(jdbc/execute! db-conn
               ["DELETE FROM students
                  WHERE student_id = ?" 3])

;; SELECT * FROM students
;; WHERE age > 20 AND grade = 'A'

(jdbc/query db-conn
            ["SELECT * FROM students
               WHERE age > 20
                 AND grade = 'A'"])

;; CREATE INDEX idx_students_grade ON students (grade);

;; DROP TABLE students

;; SELECT AVG (age) AS average_age, MAX (age) AS max_age
;; FROM students

(jdbc/query db-conn
            ["SELECT AVG (age) AS average_age,
                     MAX (age) AS max_age
                FROM students"])


;; CREATE TABLE courses (
;;     course_id SERIAL PRIMARY KEY,
;;     course_name VARCHAR(50)
;; )

(jdbc/execute! db-conn
               ["CREATE TABLE courses (
                    course_id SERIAL PRIMARY KEY,
                    course_name VARCHAR(50))"])

;; INSERT INTO courses (course_name)
;; VALUES
;;     ('Math'),
;;     ('English'),
;;     ('History')

(jdbc/execute! db-conn
               ["INSERT INTO courses (course_name)
VALUES
    ('Math'),
    ('English'),
    ('History')"])

;; CREATE TABLE enrollments (
;;     enrollment_id SERIAL PRIMARY KEY,
;;     student_id INT,
;;     course_id INT,
;;     FOREIGN KEY (student_id) REFERENCES students(student_id),
;;     FOREIGN KEY (course_id) REFERENCES courses(course_id)
;; )

(jdbc/execute! db-conn
               ["CREATE TABLE enrollments (
                  enrollment_id SERIAL PRIMARY KEY,
                  student_id INT,
                  course_id INT,
                  FOREIGN KEY (student_id) REFERENCES students(student_id),
                  FOREIGN KEY (course_id) REFERENCES courses(course_id))"])

;; INSERT INTO enrollments (student_id, course_id)
;; VALUES
;;     (1, 1),
;;     (2, 2),
;;     (3, 3)

(jdbc/execute! db-conn
            ["INSERT INTO enrollments (student_id, course_id)
VALUES
    (1, 1),
    (2, 2),
    (4, 3)
"])

;; SELECT students.first_name, students.last_name, courses.course_name
;; FROM students
;; JOIN enrollments ON students.student_id = enrollments.student_id
;; JOIN courses ON enrollments.course_id = courses.course_id

(jdbc/query db-conn
            ["SELECT students.first_name, students.last_name, courses.course_name
                FROM students
                LEFT JOIN enrollments ON students.student_id = enrollments.student_id
                LEFT JOIN courses ON enrollments.course_id = courses.course_id"])