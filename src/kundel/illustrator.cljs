(ns kundel.illustrator
  (:require
    [goog.object :as go]
    [reagent.core :as r]
    [kundel.component :as c]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; w3c custom element registration and callback handlers.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Registration occurs by calling the exported 'register' below.

;; The registered element will have the following name:
(def element-name "illustrator-component")

;; The registered element works with the following attributes.
;;
;; NOTE:
;;   A reagent atom is created for each of the component's attributes.
;;
;;   The reagent component render function gets 'element' and 'attrs'
;;   properties.  The 'attrs' property contains the ratom with these
;;   element properties.  To read the value in the reagent component:
;;
;;      @(get attrs "some-text")
;;
;;   Note that each 'attrs' has to have a corresponding 'fns' entry below.
;;
;; Modify these to suite your element:
(defn ctor-attrs []
  {"sections" (atom nil)
   "next-hint" (atom nil)
   "font-size-min--section" (atom nil)
   "font-size-max--section" (atom nil)})

;; Custom translation functions for each attribute.
;; %1 is original property value, %2 is the new value.
;; Examples:
;;    #(do %2)              ;; just replaces old value.
;;    #(.parse js/JSON %2)  ;; parses JSON into JS object
;;    #(= "true" (str %2))        ;; parses boolean
;; This list's keys must match the 'attrs' list.
(def fns {"sections" #(js->clj (.parse js/JSON %2) :keywordize-keys true)
          "next-hint" #(if %2 (str %2) nil)
          "font-size-min--section" #(do %2)
          "font-size-max--section" #(do %2)})

;; events:  "timeline" :: event detail is {"id":string}.
;;
;; timeline is at "id" provided
;; "id" of "PREVIOUS_BEFORE_FIRST" means an attempt to go to a previous flow while at first flow was triggered
;; "id" of "NEXT_AFTER_LAST" means an attempt to go to next flow while at last flow was triggered





;; NO NEED TO MODIFY ANYTHING BELOW

(def attrs (atom {}))

(defn ^:export created [this]
  (let [_attrs (ctor-attrs)]
    (doseq [keyval _attrs]
      (swap! (val keyval) (get fns (key keyval)) (.getAttribute this (key keyval))))
    (reset! attrs (merge @attrs {this _attrs}))
    (r/render [c/render this _attrs] this)))      ;; attach reagent component

(defn attached [this]) ;; not wired into reagent component

(defn ^:export detached [this] ;; not wired into reagent component, remove attributes tracked by this namespace for 'this'
  (swap! attrs (dissoc @attrs this)))

(defn ^:export changed [this property-name old-value new-value]
  (when-let [_attrs (get @attrs this)]
    (swap! (get _attrs property-name) (get fns property-name) new-value)
    (reset! attrs (merge @attrs {this _attrs}))))

;; register the w3c custom element.
(defn ^:export register []
  (when (.-registerElement js/document)
    (let [proto (.create js/Object (.-prototype js/HTMLElement))
          proto' (go/create "createdCallback" #(this-as this (created this))
                            "attachedCallback" #(this-as this (attached this))
                            "detachedCallback" #(this-as this (detached this))
                            "attributeChangedCallback" #(this-as this (changed this %1 %2 %3)))]
      (go/extend proto proto')
      (.registerElement js/document element-name #js{"prototype" proto}))))

(defn ^:export toggle [this]
  (c/toggle this))

(defn ^:export goto-start [this]
  (c/goto-start this))

(defn ^:export goto-next [this]
  (c/goto-next this))

(defn ^:export goto-previous [this]
  (c/goto-previous this))

(defn ^:export goto-section [this id]
  (c/goto-section this id))