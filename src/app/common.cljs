(ns app.common)

(defn storage-available?
  [type]
  (let [storage (atom nil)
        test    "__storage_test__"]
    (try
      (reset! storage (aget js/window type))
      (.setItem @storage test test)
      (.removeItem @storage test)
      true
      (catch js/Exception e
        (and (-> e
                 .-name
                 (= "QuotaExceededError"))
             (some? @storage)
             (-> @storage
                 .-length
                 pos?))))))