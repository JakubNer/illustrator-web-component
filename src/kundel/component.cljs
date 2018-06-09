(ns kundel.component
  (:require
    [reagent.core :as r]
    [debux.cs.core :refer-macros [clog dbg break]]
    [dommy.core :as dom]
    [kundel.css :as css]))

(def this (r/atom nil))
(def previous-paused-attribute (r/atom nil))

;;
;; narration / timeline variables and utilities
;;

(defn get-narration
  "Give sections as per component contract returns linked structure {:section ?? :subsection ?? :flow ?? :subsnext ?? :subsprev ??} for each flow from each section and each subsection; in order."
  ([sections]
   (get-narration
     sections
     (:flows (first sections))
     (:subsections (first sections))
     (:flows (first (:subsections (first sections))))
     nil))
  ([sects flows subs subflows subsprev]
   (if (seq flows)
     (cons {:section (first sects) :subsection nil :flow (first flows) :subsnext nil :subsprev nil}
           (get-narration sects (rest flows) subs subflows subsprev))
     (if (seq subflows)
       (cons {:section (first sects) :subsection (first subs) :flow (first subflows) :subsnext (second subs) :subsprev subsprev}
             (get-narration sects nil subs (rest subflows) subsprev))
       (if (seq subs)
         (get-narration sects nil (rest subs) (:flows (second subs)) (first subs))
         (when (seq sects)
           (get-narration (rest sects))))))))

(defn get-narration-keyframe [narration flow]
  "retrieves narration keyframe for flow."
  (first (keep-indexed #(when (= flow (:flow %2)) %1) narration)))

(defn get-subsection-index-percentage-tuple [narration flow]
  "Retrieve 0-based index of flow in subsection, if any, or nil.  Retrieve percentage of all subsections where subsection occurs, so 3rd subsection of 4 subsections is at 75%.  These two values are in a tuple (index, percentage)"
  (let [_keyframe (get-narration-keyframe narration flow)
        _current (nth narration _keyframe)]
    (when-let [subsection (:subsection _current)]
      (let [subscoll (:subsections (:section _current))
            index (first (keep-indexed #(when (= subsection %2) %1) subscoll))]
        (list index (* 100 (/ index (count subscoll))))))))

(def narration (r/atom nil))
(def keyframe (r/atom nil))
(def current (r/atom nil))
(def timeout (r/atom nil))

(defn get-element-id [flow]
  "Retrieve HTML ID for 'flow' element"
  (str "narrator-flow-" (:id flow)))

(defn find-parent-with-class [js-element with-class]
  "Find a JS element that's a parent of passed in 'js-element' with class 'with-class'."
  (when js-element
    (when-let [parent (dom/parent js-element)]
      (if (dom/has-class? parent with-class)
        parent
        (find-parent-with-class parent with-class)))))

(defn playing? []
  (not (nil? @timeout)))

(defn add-remove-classes-and-properties-for-animation []
  "go through all IDs and animate as required"
  (let [current-flow (:flow @current)
        current-section (:section @current)
        current-subsection (:subsection @current)
        current-flow-js-element (dom/sel1 (str "#" (get-element-id current-flow)))
        section-js-element (find-parent-with-class current-flow-js-element "narrator-section")
        sections-js-element (find-parent-with-class current-flow-js-element "narrator-sections")]
    (if (playing?)
      (dom/add-class! sections-js-element "narrating")
      (dom/remove-class! sections-js-element "narrating"))
    (doseq [rec @narration
            :let [flow (:flow rec)
                  flow-js-element (dom/sel1 (str "#"(get-element-id flow)))
                  subsection (:subsection rec)
                  section (:section rec)]
            :when flow-js-element
            :when (not= rec @current)]
      (dom/remove-class! flow-js-element "narrator-current")
      (when (and subsection (not (= subsection current-subsection)))
        (when-let [subsection-js-element (find-parent-with-class flow-js-element "narrator-subsection")]
          (dom/remove-class! subsection-js-element "narrator-current"))
        (when-let [subsection-frame-js-element (find-parent-with-class flow-js-element "narrator-subsection-frame")]
          (dom/remove-class! subsection-frame-js-element "narrator-current")))
      (when (and section (not (= section current-section)))
        (when-let [section-js-element (find-parent-with-class flow-js-element "narrator-section")]
          (dom/remove-class! section-js-element "narrator-current")
          (dom/remove-class! section-js-element "narrating-in-subsection"))))
    (when current-flow-js-element
      (dom/add-class! current-flow-js-element "narrator-current")
      (when-let [subsection-js-element (find-parent-with-class current-flow-js-element "narrator-subsection")]
        (dom/add-class! subsection-js-element "narrator-current"))
      (when-let [subsection-frame-js-element (find-parent-with-class current-flow-js-element "narrator-subsection-frame")]
        (dom/add-class! subsection-frame-js-element "narrator-current")
        (let [[_ percentage] (get-subsection-index-percentage-tuple @narration current-flow)
              carousel-js-element (find-parent-with-class current-flow-js-element "narrator-susbection-carousel")]
          (dom/set-style! carousel-js-element :transform (str "translateX(-" percentage "%)"))))
      (when sections-js-element
        (dom/add-class! section-js-element "narrator-current")
        (if current-subsection
          (dom/add-class! section-js-element "narrating-in-subsection")
          (dom/remove-class! section-js-element "narrating-in-subsection"))))))

(defn fire-event [id]
  "Dispatch 'timeline' events."
  (let [event (.createEvent js/document "Event")]
    (.initEvent event "timeline" true true)
    (aset event "id" id)
    (aset event "playing" (playing?))
    (.dispatchEvent @this event)))

(defn stop-playing []
  (when (playing?)
    (js/clearTimeout @timeout)
    (reset! timeout nil)
    (add-remove-classes-and-properties-for-animation)))

(defn set-keyframe [new-keyframe]
  (cond
    (< new-keyframe 0)
    (do
      (stop-playing)
      (reset! keyframe 0)
      (reset! current (first @narration))
      (fire-event (:id (:flow @current))))
    (>= new-keyframe (count @narration))
    (do
      (stop-playing)
      (reset! keyframe (- (count @narration) 1))
      (reset! current (last @narration))
      (fire-event (:id (:flow @current))))
    :else
    (do
      (reset! keyframe new-keyframe)
      (reset! current (nth @narration new-keyframe))
      (fire-event (:id (:flow @current)))))
  (add-remove-classes-and-properties-for-animation))

(defn start-playing []
  (stop-playing)
  (let [seconds (:seconds (:flow (nth @narration @keyframe)))
        millis (* 1000 seconds)]
    (reset! timeout (js/setTimeout #(do
                                      (when (playing?)
                                        (set-keyframe (+ 1 @keyframe))
                                        (when (playing?)
                                          (start-playing))))
                                   millis))
    (add-remove-classes-and-properties-for-animation)))

(defn pause []
  (stop-playing)
  (fire-event (:id (:flow @current)))
  (let [image-element (dom/sel1 :#narrator-sections-center-overlay)]
    (dom/remove-class! image-element :narrator-sections-center-pause)
    (dom/remove-class! image-element :narrator-sections-center-play)
    (js/setTimeout #(dom/add-class! image-element :narrator-sections-center-pause)) 100))

(defn play []
  (start-playing)
  (fire-event (:id (:flow @current)))
  (let [image-element (dom/sel1 :#narrator-sections-center-overlay)]
    (dom/remove-class! image-element :narrator-sections-center-pause)
    (dom/remove-class! image-element :narrator-sections-center-play)
    (js/setTimeout #(dom/add-class! image-element :narrator-sections-center-play) 100)))

(defn clicked-flow [flow]
  (let [keyframe_of_flow (get-narration-keyframe @narration flow)]
    (set-keyframe keyframe_of_flow)
    (when (playing?) (pause))))

(defn goto-first-subsection []
  "Set keyframe at first subsection flow of current flow"
  (when-let [first-subsection-flow (first (:flows (first (:subsections (:section @current)))))]
    (when-let [first-subsection-keyframe (get-narration-keyframe @narration first-subsection-flow)]
      (set-keyframe first-subsection-keyframe))))

;;
;; Rendered components
;;

(defn render-buttons []
  [:div.narrator-buttons
   [:img.narrator-button
    {:src "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQyMCA0MjAiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQyMCA0MjA7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMzJweCIgaGVpZ2h0PSIzMnB4Ij4KPGc+Cgk8cGF0aCBkPSJNMjEwLDIxYzEwNC4yMTYsMCwxODksODQuNzg0LDE4OSwxODlzLTg0Ljc4NCwxODktMTg5LDE4OVMyMSwzMTQuMjE2LDIxLDIxMFMxMDUuNzg0LDIxLDIxMCwyMSBNMjEwLDAgICBDOTQuMDMxLDAsMCw5NC4wMjQsMCwyMTBzOTQuMDMxLDIxMCwyMTAsMjEwczIxMC05NC4wMjQsMjEwLTIxMFMzMjUuOTY5LDAsMjEwLDBMMjEwLDB6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMTI5LjUsMTQzLjk0MXYxMzIuMTI1YzAsMTkuMjUsMTUuNzUsMzUsMzUsMzVzMzUtMTUuNzUsMzUtMzV2LTI3LjYxNWw1NS44NTMsMzYuNTU0ICAgQzI3NC42ODcsMjk3LjY0NywyOTAuNSwyODkuMSwyOTAuNSwyNjZWMTQ3Ljg3NWMwLTIzLjEtMTUuNzkyLTMxLjYxMi0zNS4wODQtMTguOTE0TDE5OS41LDE2NS43NnYtMjEuODE5YzAtMTkuMjUtMTUuNzUtMzUtMzUtMzUgICBTMTI5LjUsMTI0LjY5MSwxMjkuNSwxNDMuOTQxeiBNMTUwLjUsMTQzLjk0MWMwLTcuNyw2LjMtMTQsMTQtMTRzMTQsNi4zLDE0LDE0djIxLjYzN2MwLDcuNywwLDE5LjY1NiwwLDI2LjU3MiAgIGMwLDYuOTA5LDUuMjY0LDkuMSwxMS42OTcsNC44NjVsNjcuNjA2LTQ0LjQ3OGM2LjQzMy00LjIyOCwxMS42OTctMS4zOTMsMTEuNjk3LDYuM3Y5Ni4zMjdjMCw3LjctNS4yNzEsMTAuNTQ5LTExLjcxOCw2LjMzNSAgIGwtNjcuNTY0LTQ0LjIyNmMtNi40NDctNC4yMTQtMTEuNzE4LTIuMDE2LTExLjcxOCw0Ljg4NmMwLDYuODk1LDAsMTguODQ0LDAsMjYuNTQ0djI3LjM2M2MwLDcuNy02LjMsMTQtMTQsMTRzLTE0LTYuMy0xNC0xNCAgIFYyMzAuMzdjMC03LjcsMC0xOC4xMzcsMC0yMy4xOTFzMC0xNS40OTEsMC0yMy4xOTFWMTQzLjk0MXoiIGZpbGw9IiMwMDAwMDAiLz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K"
     :on-click #(do
                  (when (playing?) (pause))
                  (set-keyframe (max 0 (- @keyframe 1))))}]
   (if (playing?)
     [:img.narrator-button
      {:src "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQyMCA0MjAiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQyMCA0MjA7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMzJweCIgaGVpZ2h0PSIzMnB4Ij4KPGc+Cgk8cGF0aCBkPSJNMjEwLDIxYzEwNC4yMTYsMCwxODksODQuNzg0LDE4OSwxODlzLTg0Ljc4NCwxODktMTg5LDE4OVMyMSwzMTQuMjE2LDIxLDIxMFMxMDUuNzg0LDIxLDIxMCwyMSBNMjEwLDAgICBDOTQuMDMxLDAsMCw5NC4wMjQsMCwyMTBzOTQuMDMxLDIxMCwyMTAsMjEwczIxMC05NC4wMjQsMjEwLTIxMFMzMjUuOTY5LDAsMjEwLDBMMjEwLDB6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8Zz4KCQk8cGF0aCBkPSJNMjU5LDEwOC45NDFjLTE5LjI1LDAtMzUsMTUuNzUtMzUsMzV2MTMyLjEyNWMwLDE5LjI1LDE1Ljc1LDM1LDM1LDM1czM1LTE1Ljc1LDM1LTM1VjE0My45NDEgICAgQzI5NCwxMjQuNjkxLDI3OC4yNSwxMDguOTQxLDI1OSwxMDguOTQxeiBNMjczLDI3Ni4wNjZjMCw3LjctNi4zLDE0LTE0LDE0cy0xNC02LjMtMTQtMTRWMTQzLjk0MWMwLTcuNyw2LjMtMTQsMTQtMTQgICAgczE0LDYuMywxNCwxNFYyNzYuMDY2eiIgZmlsbD0iIzAwMDAwMCIvPgoJCTxwYXRoIGQ9Ik0xNjEsMTA4Ljk0MWMtMTkuMjUsMC0zNSwxNS43NS0zNSwzNXYxMzIuMTI1YzAsMTkuMjUsMTUuNzUsMzUsMzUsMzVzMzUtMTUuNzUsMzUtMzVWMTQzLjk0MSAgICBDMTk2LDEyNC42OTEsMTgwLjI1LDEwOC45NDEsMTYxLDEwOC45NDF6IE0xNzUsMjc2LjA2NmMwLDcuNy02LjMsMTQtMTQsMTRzLTE0LTYuMy0xNC0xNFYxNDMuOTQxYzAtNy43LDYuMy0xNCwxNC0xNCAgICBzMTQsNi4zLDE0LDE0VjI3Ni4wNjZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg=="
       :on-click #(pause)}]
     [:img.narrator-button
      {:src "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQyMCA0MjAiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQyMCA0MjA7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMzJweCIgaGVpZ2h0PSIzMnB4Ij4KPGc+Cgk8cGF0aCBkPSJNMjEwLDIxYzEwNC4yMTYsMCwxODksODQuNzg0LDE4OSwxODlzLTg0Ljc4NCwxODktMTg5LDE4OVMyMSwzMTQuMjE2LDIxLDIxMFMxMDUuNzg0LDIxLDIxMCwyMSBNMjEwLDAgICBDOTQuMDMxLDAsMCw5NC4wMjQsMCwyMTBzOTQuMDMxLDIxMCwyMTAsMjEwczIxMC05NC4wMjQsMjEwLTIxMFMzMjUuOTY5LDAsMjEwLDBMMjEwLDB6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMjkzLjkwOSwxODcuMjE1bC0xMTEuODE4LTczLjU5MUMxNjIuNzkyLDEwMC45MjYsMTQ3LDEwOS40NDUsMTQ3LDEzMi41NDVWMjg3LjQyYzAsMjMuMSwxNS44MTMsMzEuNjQ3LDM1LjE0NywxOC45OTggICBMMjkzLjg2LDIzMy4zMUMzMTMuMTg3LDIyMC42NDcsMzEzLjIwOCwxOTkuOTEzLDI5My45MDksMTg3LjIxNXogTTI3OS4wMDYsMjE3Ljg2OGwtOTkuMjk1LDY0Ljk4MSAgIGMtNi40NCw0LjIyMS0xMS43MTEsMS4zNzItMTEuNzExLTYuMzI4VjE0My40MzdjMC03LjcsNS4yNjQtMTAuNTM1LDExLjY5Ny02LjNsOTkuMzMsNjUuMzY2ICAgQzI4NS40NiwyMDYuNzMxLDI4NS40NTMsMjEzLjY0NywyNzkuMDA2LDIxNy44Njh6IiBmaWxsPSIjMDAwMDAwIi8+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg=="
       :on-click #(play)}])
   [:img.narrator-button
    {:src "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQyMCA0MjAiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDQyMCA0MjA7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMzJweCIgaGVpZ2h0PSIzMnB4Ij4KPGc+Cgk8cGF0aCBkPSJNMjEwLDIxYzEwNC4yMTYsMCwxODksODQuNzg0LDE4OSwxODlzLTg0Ljc4NCwxODktMTg5LDE4OVMyMSwzMTQuMjE2LDIxLDIxMFMxMDUuNzg0LDIxLDIxMCwyMSBNMjEwLDAgICBDOTQuMDMxLDAsMCw5NC4wMjQsMCwyMTBzOTQuMDMxLDIxMCwyMTAsMjEwczIxMC05NC4wMjQsMjEwLTIxMFMzMjUuOTY5LDAsMjEwLDBMMjEwLDB6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMjU1LjUsMTA4Ljk0MWMtMTkuMjUsMC0zNSwxNS43NS0zNSwzNXYyMS44MTlsLTU1LjkxNi0zNi43OTljLTE5LjI5Mi0xMi42OTEtMzUuMDg0LTQuMTg2LTM1LjA4NCwxOC45MTRWMjY2ICAgYzAsMjMuMSwxNS44MTMsMzEuNjQ3LDM1LjE0NywxOC45OThsNTUuODUzLTM2LjU1NHYyNy42MTVjMCwxOS4yNSwxNS43NSwzNSwzNSwzNXMzNS0xNS43NSwzNS0zNVYxNDMuOTQxICAgQzI5MC41LDEyNC42OTEsMjc0Ljc1LDEwOC45NDEsMjU1LjUsMTA4Ljk0MXogTTI2OS41LDE4My45OTVjMCw3LjcsMCwxOC4xMzcsMCwyMy4xOTFjMCw1LjA1NCwwLDE1LjQ5MSwwLDIzLjE5MXY0NS42ODkgICBjMCw3LjctNi4zLDE0LTE0LDE0cy0xNC02LjMtMTQtMTR2LTI3LjM2M2MwLTcuNywwLTE5LjY0OSwwLTI2LjU0NGMwLTYuOTAyLTUuMjcxLTkuMS0xMS43MTgtNC44ODZsLTY3LjU2NCw0NC4yMjYgICBjLTYuNDQ3LDQuMjE0LTExLjcxOCwxLjM1OC0xMS43MTgtNi4zMzVWMTU4LjgzYzAtNy43LDUuMjY0LTEwLjUzNSwxMS42OTctNi4zbDY3LjYwNiw0NC40NzggICBjNi40MzMsNC4yMjgsMTEuNjk3LDIuMDQ0LDExLjY5Ny00Ljg2NWMwLTYuOTE2LDAtMTguODcyLDAtMjYuNTcydi0yMS42MzdjMC03LjcsNi4zLTE0LDE0LTE0czE0LDYuMywxNCwxNFYxODMuOTk1eiIgZmlsbD0iIzAwMDAwMCIvPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+CjxnPgo8L2c+Cjwvc3ZnPgo="
     :on-click #(do
                  (when (playing?) (pause))
                  (set-keyframe (min (- (count @narration) 1) (+ @keyframe 1))))}]])

(defn render-sections [sections]
  [:div.narrator-sections
    [:div.narrator-sections-center
     [:div#narrator-sections-center-overlay]]
    (doall
      (for [section sections]
        [:div.narrator-section {:key (gensym "n-sct-")
                                :class [(when (:subsections section) "has-narrator-subsections")]}
         (doall
           (for [flow (:flows section)]
             [:span.narrator-flow {:key                     (gensym "n-sct-fl-")
                                   :id                      (get-element-id flow)
                                   :dangerouslySetInnerHTML #js{:__html (:html flow)}
                                   :on-click                #(clicked-flow flow)}]))
         [:div.narrator-subsection-frame-expand
          {:on-click #(goto-first-subsection)}
          [:img
           {:src      "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDU1IDU1IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCA1NSA1NTsiIHhtbDpzcGFjZT0icHJlc2VydmUiIHdpZHRoPSIzMnB4IiBoZWlnaHQ9IjMycHgiPgo8Zz4KCTxwYXRoIGQ9Ik01NC45MjQsMjQuMzgyYzAuMTAxLTAuMjQ0LDAuMTAxLTAuNTE5LDAtMC43NjRjLTAuMDUxLTAuMTIzLTAuMTI1LTAuMjM0LTAuMjE3LTAuMzI3TDQyLjcwOCwxMS4yOTMgICBjLTAuMzkxLTAuMzkxLTEuMDIzLTAuMzkxLTEuNDE0LDBzLTAuMzkxLDEuMDIzLDAsMS40MTRMNTEuNTg3LDIzSDM2LjAwMVYxYzAtMC41NTMtMC40NDctMS0xLTFoLTM0ICAgYy0wLjAzMiwwLTAuMDYsMC4wMTUtMC4wOTEsMC4wMThDMC44NTQsMC4wMjMsMC44MDUsMC4wMzYsMC43NTIsMC4wNUMwLjY1OCwwLjA3NSwwLjU3NCwwLjEwOSwwLjQ5MywwLjE1OCAgIEMwLjQ2NywwLjE3NCwwLjQzNSwwLjE3NCwwLjQxMSwwLjE5MkMwLjM4LDAuMjE1LDAuMzU2LDAuMjQ0LDAuMzI4LDAuMjY5Yy0wLjAxNywwLjAxNi0wLjAzNSwwLjAzLTAuMDUxLDAuMDQ3ICAgQzAuMjAxLDAuMzk4LDAuMTM5LDAuNDg5LDAuMDkzLDAuNTg5Yy0wLjAwOSwwLjAyLTAuMDE0LDAuMDQtMC4wMjIsMC4wNkMwLjAyOSwwLjc2MSwwLjAwMSwwLjg3OCwwLjAwMSwxdjQ2ICAgYzAsMC4xMjUsMC4wMjksMC4yNDMsMC4wNzIsMC4zNTVjMC4wMTQsMC4wMzcsMC4wMzUsMC4wNjgsMC4wNTMsMC4xMDNjMC4wMzcsMC4wNzEsMC4wNzksMC4xMzYsMC4xMzIsMC4xOTYgICBjMC4wMjksMC4wMzIsMC4wNTgsMC4wNjEsMC4wOSwwLjA5YzAuMDU4LDAuMDUxLDAuMTIzLDAuMDkzLDAuMTkzLDAuMTNjMC4wMzcsMC4wMiwwLjA3MSwwLjA0MSwwLjExMSwwLjA1NiAgIGMwLjAxNywwLjAwNiwwLjAzLDAuMDE4LDAuMDQ3LDAuMDI0bDIyLDdDMjIuNzk3LDU0Ljk4NCwyMi44OTksNTUsMjMuMDAxLDU1YzAuMjEsMCwwLjQxNy0wLjA2NiwwLjU5LTAuMTkyICAgYzAuMjU4LTAuMTg4LDAuNDEtMC40ODgsMC40MS0wLjgwOHYtNmgxMWMwLjU1MywwLDEtMC40NDcsMS0xVjI1aDE1LjU4Nkw0MS4yOTQsMzUuMjkzYy0wLjM5MSwwLjM5MS0wLjM5MSwxLjAyMywwLDEuNDE0ICAgQzQxLjQ4OSwzNi45MDIsNDEuNzQ1LDM3LDQyLjAwMSwzN3MwLjUxMi0wLjA5OCwwLjcwNy0wLjI5M2wxMS45OTktMTEuOTk5QzU0Ljc5OSwyNC42MTYsNTQuODczLDI0LjUwNSw1NC45MjQsMjQuMzgyeiAgICBNMjIuMDAxLDUyLjYzM2wtMjAtNi4zNjRWMi4zNjdsMjAsNi4zNjRWNTIuNjMzeiBNMzQuMDAxLDQ2aC0xMFY4YzAtMC40MzYtMC4yODItMC44MjEtMC42OTctMC45NTNMNy40NDIsMmgyNi41NTlWNDZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMjAuMzcyLDMxLjA3MWwtNS0yYy0wLjUwOS0wLjIwNS0xLjA5NSwwLjA0My0xLjMsMC41NThjLTAuMjA1LDAuNTEzLDAuMDQ1LDEuMDk1LDAuNTU4LDEuM2w1LDIgICBDMTkuNzUxLDMyLjk3OCwxOS44NzcsMzMsMjAuMDAxLDMzYzAuMzk2LDAsMC43NzItMC4yMzcsMC45MjktMC42MjlDMjEuMTM0LDMxLjg1OCwyMC44ODQsMzEuMjc2LDIwLjM3MiwzMS4wNzF6IiBmaWxsPSIjMDAwMDAwIi8+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg=="}]
          [:img
           {:src      "data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDU5IDU5IiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCA1OSA1OTsiIHhtbDpzcGFjZT0icHJlc2VydmUiIHdpZHRoPSIzMnB4IiBoZWlnaHQ9IjMycHgiPgo8Zz4KCTxwYXRoIGQ9Ik0yNi41LDJjMS42NTQsMCwzLDEuMzQ2LDMsM3MtMS4zNDYsMy0zLDNjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFzMC40NDgsMSwxLDFjMi43NTcsMCw1LTIuMjQzLDUtNXMtMi4yNDMtNS01LTUgICBjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFTMjUuOTQ4LDIsMjYuNSwyeiIgZmlsbD0iIzAwMDAwMCIvPgoJPHBhdGggZD0iTTMyLjUsMmMxLjY1NCwwLDMsMS4zNDYsMywzcy0xLjM0NiwzLTMsM2MtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWMyLjc1NywwLDUtMi4yNDMsNS01cy0yLjI0My01LTUtNSAgIGMtMC41NTIsMC0xLDAuNDQ3LTEsMVMzMS45NDgsMiwzMi41LDJ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNNDMuMzk5LDRjLTAuNDY1LTIuMjc5LTIuNDg0LTQtNC44OTktNGMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWMxLjY1NCwwLDMsMS4zNDYsMywzcy0xLjM0NiwzLTMsMyAgIGMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWMyLjQxNCwwLDQuNDM0LTEuNzIxLDQuODk5LTRINTMuNXYxMGgtMWMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgxdjhoLTEgICBjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFzMC40NDgsMSwxLDFoMXY4aC0xYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDF2N2gtMTJ2MTJoLTM2di05aDFjMC41NTIsMCwxLTAuNDQ3LDEtMXMtMC40NDgtMS0xLTEgICBoLTF2LThoMWMwLjU1MiwwLDEtMC40NDcsMS0xcy0wLjQ0OC0xLTEtMWgtMXYtOGgxYzAuNTUyLDAsMS0wLjQ0NywxLTFzLTAuNDQ4LTEtMS0xaC0xdi04aDFjMC41NTIsMCwxLTAuNDQ3LDEtMXMtMC40NDgtMS0xLTFoLTEgICBWNmgxMWgzYzAuNTUyLDAsMS0wLjQ0NywxLTFzLTAuNDQ4LTEtMS0xaC0xLjgxNmMwLjQxNC0xLjE2MiwxLjUxNC0yLDIuODE2LTJjMS42NTQsMCwzLDEuMzQ2LDMsM3MtMS4zNDYsMy0zLDMgICBjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFzMC40NDgsMSwxLDFjMi43NTcsMCw1LTIuMjQzLDUtNXMtMi4yNDMtNS01LTVjLTIuNDE0LDAtNC40MzQsMS43MjEtNC44OTksNEgzLjV2NTVoMzhoMS40MTRINTUuNVY0Ni40MTRWNDUgICBWNEg0My4zOTl6IE00My41LDQ3aDguNTg2TDQzLjUsNTUuNTg2VjQ3eiBNNDQuOTE1LDU3bDguNTg1LTguNTg1VjU3SDQ0LjkxNXoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0zMC41LDE2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMzMS4wNTIsMTYsMzAuNSwxNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0yNC41LDE2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMyNS4wNTIsMTYsMjQuNSwxNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0xOC41LDE2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMxOS4wNTIsMTYsMTguNSwxNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik00MC41LDE4aDJjMC41NTIsMCwxLTAuNDQ3LDEtMXMtMC40NDgtMS0xLTFoLTJjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFTMzkuOTQ4LDE4LDQwLjUsMTh6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNNDYuNSwxOGgyYzAuNTUyLDAsMS0wLjQ0NywxLTFzLTAuNDQ4LTEtMS0xaC0yYy0wLjU1MiwwLTEsMC40NDctMSwxUzQ1Ljk0OCwxOCw0Ni41LDE4eiIgZmlsbD0iIzAwMDAwMCIvPgoJPHBhdGggZD0iTTM2LjUsMTZoLTJjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFzMC40NDgsMSwxLDFoMmMwLjU1MiwwLDEtMC40NDcsMS0xUzM3LjA1MiwxNiwzNi41LDE2eiIgZmlsbD0iIzAwMDAwMCIvPgoJPHBhdGggZD0iTTEzLjUsMTdjMC0wLjU1My0wLjQ0OC0xLTEtMXYtNGMwLTAuNTUzLTAuNDQ4LTEtMS0xcy0xLDAuNDQ3LTEsMXY0Yy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxdjggICBjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFzMC40NDgsMSwxLDF2OGMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMXY4Yy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxdjVjMCwwLjU1MywwLjQ0OCwxLDEsMSAgIHMxLTAuNDQ3LDEtMXYtNWMwLjU1MiwwLDEtMC40NDcsMS0xcy0wLjQ0OC0xLTEtMXYtOGMwLjU1MiwwLDEtMC40NDcsMS0xcy0wLjQ0OC0xLTEtMXYtOGMwLjU1MiwwLDEtMC40NDcsMS0xcy0wLjQ0OC0xLTEtMXYtOCAgIEMxMy4wNTIsMTgsMTMuNSwxNy41NTMsMTMuNSwxN3oiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0zNi41LDI2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMzNy4wNTIsMjYsMzYuNSwyNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0zMC41LDI2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMzMS4wNTIsMjYsMzAuNSwyNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0xOC41LDI2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMxOS4wNTIsMjYsMTguNSwyNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik0yNC41LDI2aC0yYy0wLjU1MiwwLTEsMC40NDctMSwxczAuNDQ4LDEsMSwxaDJjMC41NTIsMCwxLTAuNDQ3LDEtMVMyNS4wNTIsMjYsMjQuNSwyNnoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik00Ni41LDI4aDJjMC41NTIsMCwxLTAuNDQ3LDEtMXMtMC40NDgtMS0xLTFoLTJjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFTNDUuOTQ4LDI4LDQ2LjUsMjh6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNNDAuNSwyOGgyYzAuNTUyLDAsMS0wLjQ0NywxLTFzLTAuNDQ4LTEtMS0xaC0yYy0wLjU1MiwwLTEsMC40NDctMSwxUzM5Ljk0OCwyOCw0MC41LDI4eiIgZmlsbD0iIzAwMDAwMCIvPgoJPHBhdGggZD0iTTQwLjUsMzhoMmMwLjU1MiwwLDEtMC40NDcsMS0xcy0wLjQ0OC0xLTEtMWgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMVMzOS45NDgsMzgsNDAuNSwzOHoiIGZpbGw9IiMwMDAwMDAiLz4KCTxwYXRoIGQ9Ik00Ni41LDM4aDJjMC41NTIsMCwxLTAuNDQ3LDEtMXMtMC40NDgtMS0xLTFoLTJjLTAuNTUyLDAtMSwwLjQ0Ny0xLDFTNDUuOTQ4LDM4LDQ2LjUsMzh6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMzYuNSwzNmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMzcuMDUyLDM2LDM2LjUsMzZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMzAuNSwzNmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMzEuMDUyLDM2LDMwLjUsMzZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMTguNSwzNmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMTkuMDUyLDM2LDE4LjUsMzZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMjQuNSwzNmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMjUuMDUyLDM2LDI0LjUsMzZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMjQuNSw0NmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMjUuMDUyLDQ2LDI0LjUsNDZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMzYuNSw0NmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMzcuMDUyLDQ2LDM2LjUsNDZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMzAuNSw0NmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMzEuMDUyLDQ2LDMwLjUsNDZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8cGF0aCBkPSJNMTguNSw0NmgtMmMtMC41NTIsMC0xLDAuNDQ3LTEsMXMwLjQ0OCwxLDEsMWgyYzAuNTUyLDAsMS0wLjQ0NywxLTFTMTkuMDUyLDQ2LDE4LjUsNDZ6IiBmaWxsPSIjMDAwMDAwIi8+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg=="}]]
         [:div.narrator-subsection-frame
          [:img.narrator-subsection-frame-left
           {:style    {:disabled true}
            :src      ""
            :on-click #()}]
          [:img.narrator-subsection-frame-right
           {:style    {:disabled true}
            :src      ""
            :on-click #()}]
          [:div.narrator-susbection-carousel
           {:style {:width (str (* 100 (count (:subsections section))) "%")}}
           (doall
             (for [subsection (:subsections section)]
               [:div.narrator-subsection {:key (gensym "n-ssct-")}
                (doall
                  (for [flow (:flows subsection)]
                    [:span.narrator-flow {:key                     (gensym "n-sct-fl-")
                                          :id                      (get-element-id flow)
                                          :dangerouslySetInnerHTML #js{:__html (:html flow)}
                                          :on-click                #(clicked-flow flow)}]))]))]]]))])

(defn timeline-render [sections]
  [:div.narrator-frame
   [render-sections sections]
   [render-buttons]])

(defn render [_this attrs]
  (let [sections @(get attrs "sections")
        paused  @(get attrs "paused")
        previous-paused @previous-paused-attribute
        font-min @(get attrs "font-size-min--section")
        font-max @(get attrs "font-size-max--section")
        triggered @(get attrs "trigger")] ;; whenever triggered, make divs not visible, and animate visible after 500ms]
    (reset! this _this)
    (reset! narration (get-narration sections))
    (if triggered
      (do
        (reset! (get attrs "trigger") false)
        (js/setTimeout #(do
                          (set-keyframe 0)
                          (when (not paused) (play)) 1000)))
      (when (not= paused previous-paused)
        (if paused (when (playing?) (pause)) (when (not (playing?)) (play)))
        (reset! previous-paused-attribute paused)))
    [:div
     [:style (css/get-styles font-min font-max)]
     (timeline-render sections)]))