(ns skkime_kana_reg_maker.readwrite)

;;;;;;;;;;;;;;;;;;;
;; encoder

(require '[clojure.java.io :as io])
(require '[clojure.string :as st])

;;;;;;;;;;;;;;;;;;;
;; inputfile
;; <input stroke>|<hiragana>|<katancana>|<next-string>
;;   k'|Ç©Ç¢|ÉJÉC|-
;;  note: <next-string> is not supported now.
;;

(def separator #"\|")

(defn- char-to-doublehex [ch]
  (let [dat (reverse
             (map #(apply str %)
                  (partition 2 (seq (Integer/toHexString (int ch))))))]
    (if (even? (count dat))
      dat
      (conj (vec dat) "00"))))

(defn- encode-data [infile]
  (for [data (-> (slurp infile)
                 (st/replace-first ,, #"\ufeff" "")  ;; delete BOM of UTF-8
                 (st/split-lines ,,))]
    (let [[inp hira kata] (st/split data separator)]
      (st/join ","
               (concat  ["31" "00"] (mapcat char-to-doublehex (seq inp))
                        ["5c" "00" "30" "00"]
                        ["5c" "00" "30" "00" "30" "00"]
                        ["5c" "00" "30" "00"] (mapcat char-to-doublehex (seq hira))
                        ["5c" "00" "30" "00"] (mapcat char-to-doublehex (seq kata))
                        ["5c" "00" "30" "00" "00" "00,"])))))

(defn encode-outf [infile outfile]
  (with-open  [outs (io/writer outfile)]
    (dorun (for [data (encode-data infile)]
             (do
               (.write outs data)
               (.write outs "\n"))))))

(defn encode [infile]
  (doall
   (for [data (encode-data infile)]
     data)))
