(ns nylle.polylabel)

(defn bounding-box
  "Returns the bounding box of the specified polygon"
  [polygon]
  (let [lnglats (first polygon)]
    (reduce
     (fn [acc curr]
       (let [[x y] curr]
         {:min-x (min x (or (:min-x acc) x))
          :min-y (min y (or (:min-y acc) y))
          :max-x (max x (or (:max-x acc) x))
          :max-y (max y (or (:max-y acc) y))}))
     {}
     lnglats)))

(defn point-to-seg-dist-sq
  "Returns the squared distance from a point to a segment"
  [px py a b]
  (let [sqrsum       (fn [dx dy] (+ (* dx dx) (* dy dy)))
        [ax ay]      a
        [bx by]      b
        dx           (- bx ax)
        dy           (- by ay)
        default      (sqrsum (- px ax) (- py ay))]
    (if (or (not= 0 dx) (not= 0 dy))
      (let [t (/ (+ (* (- px ax) dx) (* (- py ay) dy)) (sqrsum dx dy))]
        (cond
         (> t 1) (sqrsum (- px bx) (- py by))
         (> t 0) (sqrsum (- px (+ ax (* dx t))) (- py (+ ay (* dy t))))
         :else   default))
      default)))

(defn point-to-poly-dist
  "Returns the signed distance from a point to the polygon outline (negative if point is outside)"
  [x y polygon]
  (let [lnglats         (first polygon)
        lnglats-rotated (conj (vec (rest lnglats)) (first lnglats))
        min-dist-sq     (reduce
                         (fn [acc curr]
                           (let [min    (min (:seg-dist-sq acc) (:seg-dist-sq curr))
                                 inside (if (:inside curr) (not (:inside acc)) (:inside acc))]
                             {:inside inside :seg-dist-sq min}))
                         {:inside false :seg-dist-sq ##Inf}
                         (mapv
                          (fn [a b]
                            (let [[ax ay] a
                                  [bx by] b]
                              {:inside      (and
                                             (not= (> ay y) (> by y))
                                             (< x (+ (/ (* (- bx ax) (- y ay)) (- by ay)) ax)))
                               :seg-dist-sq (point-to-seg-dist-sq x y a b)}))
                          lnglats
                          lnglats-rotated))]
    (* (Math/sqrt (:seg-dist-sq min-dist-sq)) (if (:inside min-dist-sq) 1 -1))))

(defn new-cell
  "Returns a new cell"
  [x y h polygon]
  (let [d   (point-to-poly-dist x y polygon)
        max (+ d (* h Math/SQRT2))]
    {:x   x
     :y   y
     :h   h
     :d   d
     :max max}))

(defn centroid-cell
  "Returns the centroid cell for the specified polygon"
  [polygon]
  (let [lnglats         (first polygon)
        lnglats-rotated (conj (vec (rest lnglats)) (first lnglats))
        spec            (reduce
                         (fn [s t]
                           {:x    (+ (:x s) (:x t))
                            :y    (+ (:y s) (:y t))
                            :area (+ (:area s) (:area t))})
                         (mapv
                          (fn [a b]
                            (let [[ax ay] a
                                  [bx by] b
                                  f       (- (* ax by) (* bx ay))]
                              {:x    (* f (+ ax bx))
                               :y    (* f (+ ay by))
                               :area (* f 3)}))
                          lnglats
                          lnglats-rotated))]
    (if (= 0 (:area spec))
      (new-cell (first (first lnglats)) (first (second lnglats)) 0 polygon)
      (new-cell (/ (:x spec) (:area spec)) (/ (:y spec) (:area spec)) 0 polygon))))

(defn split-cell
  "Returns a vector of four cells by splitting the specified cell"
  [cell polygon]
  (let [h           (/ (:h cell) 2)
        {x :x y :y} cell]
    [(new-cell (- x h) (- y h) h polygon)
     (new-cell (+ x h) (- y h) h polygon)
     (new-cell (- x h) (+ y h) h polygon)
     (new-cell (+ x h) (+ y h) h polygon)]))

(defn find-best-cell
  "Returns the best cell by iterating over the specified cells"
  [cells candidate polygon precision]
  (if (empty? cells)
    candidate
    (let [head (first cells)
          tail (rest cells)
          best (if (> (:d head) (:d candidate)) head candidate)]
      (recur
        (if (<= (- (:max head) (:d best)) precision)
          tail
          (sort-by :max (into [] (concat tail (split-cell head polygon)))))
        best
        polygon
        precision))))

(defn poly-label
  "Returns the pole of inaccessibility for the specified polygon with the specified precision"
  ([polygon] (poly-label polygon 1.0))
  ([polygon precision]
   (let [{min-x :min-x
          min-y :min-y
          max-x :max-x
          max-y :max-y} (bounding-box polygon)
         width     (- max-x min-x)
         height    (- max-y min-y)
         cell-size (min width height)
         h         (/ cell-size 2)]
     (if (= 0 cell-size)
       [min-x min-y]
       (let [cells                   (for [x (range min-x max-x cell-size)
                                           y (range min-y max-y cell-size)]
                                       (new-cell (+ x h) (+ y h) h polygon))
             centroid-cell           (centroid-cell polygon)
             bounding-cell           (new-cell (+ min-x (/ width 2)) (+ min-y (/ height 2)) 0 polygon)
             best-cell-candidate     (if (> (:d bounding-cell) (:d centroid-cell)) bounding-cell centroid-cell)
             result                  (find-best-cell (sort-by :max cells) best-cell-candidate polygon precision)]
         [(:x result) (:y result)])))))