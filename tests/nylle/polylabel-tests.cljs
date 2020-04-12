(ns nylle.polylabel-tests
  (:require [nylle.polylabel :as sut]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test--bounding-box
  (testing "bounding box"
           (is
            (= {:min-x -11.5, :min-y 42.9, :max-x 3.5, :max-y 51}
               (sut/bounding-box
                [[[-11.5 51] [3.5 51] [-0.7 42.9] [-11.5 42.9] [-11.5 51]]])))))

(deftest test--point-to-seg-dist-sq
  (testing "squared distance from a point to a segment"
           (is
            (= 32.804999999999986
               (sut/point-to-seg-dist-sq
                -7.449999999999999 46.95 [-11.5 51] [-11.5 51])))
           (is
            (= 49.36159408442307
               (sut/point-to-seg-dist-sq
                -5.600000000000002 47.185344827586235 [-11.5 51] [-11.5 51])))
           (is
            (= 72.65249999999997
               (sut/point-to-seg-dist-sq
                -4 46.95 [-11.5 51] [-11.5 51])))
           (is
            (= 9
               (sut/point-to-seg-dist-sq
                -4 46.95 [-1 45.6] [-1 48.3])))))

(deftest test--point-to-poly-dist
  (testing "distance between point and polygon"
           (let [polygon [[[-11.5 51]
                           [3.5 51]
                           [3.5 50.1]
                           [2 50.1]
                           [2 49.2]
                           [0.5 49.2]
                           [0.5 48.3]
                           [-1 48.3]
                           [-1 45.6]
                           [-0.7 45.6]
                           [-0.7 42.9]
                           [-11.5 42.9]
                           [-11.5 51]]]]
             (is
              (= 4.049999999999997
                 (sut/point-to-poly-dist -7.449999999999999 46.95 polygon)))
             (is
              (= -1.358307770720607
                 (sut/point-to-poly-dist 0.6500000000000021 46.95 polygon)))
             (is
              (= 3.814655172413765
                 (sut/point-to-poly-dist -5.600000000000002 47.185344827586235 polygon)))
             (is
              (= 3
                 (sut/point-to-poly-dist -4 46.95 polygon))))))

(deftest test--centroid-cell
  (testing "centroid cell"
           (is
            (=
             {:x   -5.600000000000002
              :y   47.185344827586235
              :h   0
              :d   3.814655172413765
              :max 3.814655172413765}
             (sut/centroid-cell
              [[[-11.5 51]
                [3.5 51]
                [3.5 50.1]
                [2 50.1]
                [2 49.2]
                [0.5 49.2]
                [0.5 48.3]
                [-1 48.3]
                [-1 45.6]
                [-0.7 45.6]
                [-0.7 42.9]
                [-11.5 42.9]
                [-11.5 51]]])))
           (is
            (=
             {:x   -9.249999999999998
              :y   49.64999999999997
              :h   0
              :d   1.349999999999973
              :max 1.349999999999973}
             (sut/centroid-cell
              [[[-11.5 51] [-7 51] [-7 48.3] [-11.5 48.3] [-11.5 51]]])))))

(deftest test--poly-label
  (testing "pole of inaccessibility is found"
           (is
            (= [-7.449999999999999 46.95]
               (sut/poly-label
                [[[-11.5 51]
                  [3.5 51]
                  [3.5 50.1]
                  [2 50.1]
                  [2 49.2]
                  [0.5 49.2]
                  [0.5 48.3]
                  [-1 48.3]
                  [-1 45.6]
                  [-0.7 45.6]
                  [-0.7 42.9]
                  [-11.5 42.9]
                  [-11.5 51]]]
                10)))
           (is
            (= [17.427321814254835 57.56533477321812]
               (sut/poly-label
                [[[7.7 60.9]
                  [31.1 60.9]
                  [31.1 59.1]
                  [25.7 59.1]
                  [25.7 56.4]
                  [21.5 56.4]
                  [21.5 53.7]
                  [17 53.7]
                  [17 52.8]
                  [9.5 52.8]
                  [9.5 59.1]
                  [7.7 59.1]
                  [7.7 60.9]]]
                10)))
           (is
            (= [174.875 70.25]
               (sut/poly-label
                [[[167 74.3] [182.75 74.3] [182.75 66.2] [167 66.2] [167 74.3]]]
                10)))))