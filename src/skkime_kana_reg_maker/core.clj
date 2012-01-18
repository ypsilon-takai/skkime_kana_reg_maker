(ns skkime_kana_reg_maker.core
  (:gen-class)
  (:require [seesaw.chooser :as chooser]
            [clojure.string :as string]
            [skkime_kana_reg_maker.readwrite :as rw])
  (:use [seesaw core]))

(native!)

;;;;;;;;;;;;;;;;;;;
;; font
(def font-bold "ARIAL-18-BOLD")
(def font-normal "ARIAL-18-PLAIN")

;;;;;;;;;;;;;;;;;;;
;; file selection
(def last-visited-dir (atom (System/getProperty "user.home")))

(defn get-file []
  (chooser/choose-file
   :type :open
   :dir @last-visited-dir
   :multi? false
   :selection-mode :files-only
   :remember-directory? true
   :success-fn (fn [fc file]
                 (do
                   (reset! last-visited-dir (.getParent file))
                   file))))

;;;;;;;;;;;;;;;;;;;
;; action handler
(def inputfile-action
  (action
   :enabled? true
   :name "select"
   :handler
   (fn [e]
     (let [fd (get-file)]
       (dorun (text! (select (to-root e) [:#inputfilename]) (.getName fd))
              (text! (select (to-root e) [:#inputfilecontents]) fd)
              (text! (select (to-root e) [:#outputtextbox])
                     (str (string/join "¥¥¥n" (rw/encode fd))
                          "¥¥¥n")))))))

(def outputfile-action
  (action
   :enabled? true
   :name "select"
   :handler
   (fn [e]
     (let [fd (get-file)]
       (dorun (text! (select (to-root e) [:#outputfilename]) (.getName fd))
              (spit fd (config (select (to-root e) [:#outputtextbox]) :text)))))))

;;;;;;;;;;;;;;;;;;;
;;
(defn make-selection-field [f-name f-string actionf]
  (flow-panel
   :align :left
   :hgap 5
   :items [(label :text f-string
                  :font font-bold
                  :size [100 :by 25])
           (button :id (keyword (str f-name "button"))
                   :text "select"
                   :font font-normal
                   :size [100 :by 25]
                   :action actionf)
           (label :id (keyword (str f-name "filename"))
                  :text "none"
                  :font font-normal)]))

;;;;;;;;;;;;;;;;;;;
;; main
(defn main-window []
  (frame :id :mainframe
         :title "skkime ローマ字かなテーブルレジストリ生成ツール"
         :size [800 :by 480] 
         :resizable? true
         :on-close :dispose
         :content
         (border-panel
          :north  (make-selection-field "input"
                                        "Input file name  :"
                                        inputfile-action)
          :south  (make-selection-field "output"
                                        "Output file name :"
                                        outputfile-action)
          :center (left-right-split
                   (scrollable
                    (text
                     :id :inputfilecontents
                     :text "<input file contents>"
                     :multi-line? true
                     :editable? false
                     :margin 3))
                   (scrollable
                    (text
                     :id :outputtextbox
                     :text "<output here>"
                     :multi-line? true
                     :editable? false
                     :margin 3))))))

(defn -main [& args]
  (invoke-later
   (-> (main-window)
       show!)))