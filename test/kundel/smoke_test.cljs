(ns kundel.smoke-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [kundel.component :as s]))

(deftest get-illustration-basic
  (let [sections '({:flows (:a :b :c)
                    :subsections ({:flows (:x :y :z)}
                                  {:flows (:o)}
                                  {:flows (:l :m :n)})}
                   {:flows (:e :f :g)})]
    (testing "is a vanilla section setup creating the right illustration"
      (let [result (s/get-illustration sections)]
        (is (= result
               '(
                 {:section
                             {:flows (:a :b :c),
                              :subsections
                                     ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection nil,
                  :flow :a,
                  :subsnext nil,
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection nil,
                  :flow :b,
                  :subsnext nil,
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection nil,
                  :flow :c,
                  :subsnext nil,
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:x :y :z)},
                  :flow :x,
                  :subsnext {:flows (:o)},
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:x :y :z)},
                  :flow :y,
                  :subsnext {:flows (:o)},
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:x :y :z)},
                  :flow :z,
                  :subsnext {:flows (:o)},
                  :subsprev nil}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:o)},
                  :flow :o,
                  :subsnext {:flows (:l :m :n)},
                  :subsprev {:flows (:x :y :z)}}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:l :m :n)},
                  :flow :l,
                  :subsnext nil,
                  :subsprev {:flows (:o)}}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:l :m :n)},
                  :flow :m,
                  :subsnext nil,
                  :subsprev {:flows (:o)}}
                 {:section
                              {:flows (:a :b :c),
                               :subsections
                                      ({:flows (:x :y :z)} {:flows (:o)} {:flows (:l :m :n)})},
                  :subsection {:flows (:l :m :n)},
                  :flow :n,
                  :subsnext nil,
                  :subsprev {:flows (:o)}}
                 {:section {:flows (:e :f :g)},
                  :subsection nil,
                  :flow :e,
                  :subsnext nil,
                  :subsprev nil}
                 {:section {:flows (:e :f :g)},
                  :subsection nil,
                  :flow :f,
                  :subsnext nil,
                  :subsprev nil}
                 {:section {:flows (:e :f :g)},
                  :subsection nil,
                  :flow :g,
                  :subsnext nil,
                  :subsprev nil})))))))

(deftest get-illustration-no-flows-just-subsections
  (let [sections '({:subsections ({:flows (:x)})}
                   {:flows (:e)})]
    (testing "is a section setup without flows but with subsections OK?"
      (let [result (s/get-illustration sections)]
        (is (= result
               '({:section {:subsections ({:flows (:x)})},
                  :subsection {:flows (:x)},
                  :flow :x,
                  :subsnext nil,
                  :subsprev nil}
                 {:section {:flows (:e)},
                  :subsection nil,
                  :flow :e,
                  :subsnext nil,
                  :subsprev nil})))))))

(deftest get-illustration-one-flow
  (let [sections '({:flows (:e)})]
    (testing "is a section setup with just one flow OK?"
      (let [result (s/get-illustration sections)]
        (is (= result
               '({:section {:flows (:e)},
                  :subsection nil,
                  :flow :e,
                  :subsnext nil,
                  :subsprev nil})))))))

(deftest get-illustration-empty
  (let [sections '()]
    (testing "is a section setup with just one flow OK?"
      (let [result (s/get-illustration sections)]
        (is (= result
               nil))))))

(deftest get-illustration-keyframe-basic
  (let [sections '({:flows (:a :b :c)
                    :subsections ({:flows (:x :y :z)}
                                  {:flows (:o)}
                                  {:flows (:l :m :n)})}
                   {:flows (:e :f :g)})
        illustration (s/get-illustration sections)]
    (testing "ensure we can get keyframe of flows"
        (is (= 0 (s/get-illustration-keyframe illustration :a)))
        (is (= 3 (s/get-illustration-keyframe illustration :x)))
        (is (= 6 (s/get-illustration-keyframe illustration :o))))))
