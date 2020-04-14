# Where do I put the label in my polygon?

The [pole of inaccessibility](https://en.wikipedia.org/wiki/Pole_of_inaccessibility) of a polygon, often referred to as the "visual center" is a point _within_ the polygon with the greatest distance to any nearby border. In order to put a label on the polygon, you want to find this perfect spot as opposed to the [centroid](https://en.wikipedia.org/wiki/Centroid), the center of the smallest possible circle around the polygon, which may be outside the polygon's area.

## cljs-polylabel

The people at [mapbox](https://blog.mapbox.com/a-new-algorithm-for-finding-a-visual-center-of-a-polygon-7c77e6492fbc) came up with a new algorithm to find the pole of inaccessibility as quick as possible and shared their solution as a JavaScript-library called [polylabel](https://github.com/mapbox/polylabel).

This is the attempt to port the algorithm to ClojureScript.

## Usage
```clojurescript
user=> (def polygon [[[167 74.3] [182.75 74.3] [182.75 66.2] [167 66.2] [167 74.3]]]) ;; geoJSON
user=> (polylabel polygon) ;; using default precision of 1.0
[174.875 70.25]

user=> (polylabel polygon 10) ;; with custom precision of 10
[174.875 70.25]
```

## License

[MIT](https://github.com/Nylle/cljs-polylabel/blob/master/LICENSE.txt)
