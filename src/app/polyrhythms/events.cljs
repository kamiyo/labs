(ns app.polyrhythms.events
  (:require ["fraction.js" :as Fraction]
            [app.common :refer [storage-available?]]
            [app.events :refer [check-spec-interceptor standard-interceptors]]
            [app.polyrhythms.common
             :refer
             [get-context-current-time get-seconds-per-beat lcm]]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))

(def CURSOR-WIDTH 4)

(reg-event-fx
 :poly/fetch-local-storage
 [standard-interceptors]
 (fn [cofx [_]]
   (when (storage-available? "localStorage")
     (let [poly-tempo      (-> js/window
                               .-localStorage
                               (.getItem "poly_tempo"))
           poly-num-div    (-> js/window
                               .-localStorage
                               (.getItem "poly_num_div"))
           poly-den-div    (-> js/window
                               .-localStorage
                               (.getItem "poly_den_div"))
           poly-verbose    (-> js/window
                               .-localStorage
                               (.getItem "poly_verbose")
                               (#(.parse js/JSON %)))
           tempo-dispatch  (if (some? poly-tempo)
                             [:dispatch [:poly/set-tempo poly-tempo]]
                             nil)
           num-dispatch    (if (some? poly-num-div)
                             [:dispatch
                              [:poly/change-divisions {:divisions poly-num-div :which :numerator}]]
                             nil)
           den-dispatch    (if (some? poly-den-div)
                             [:dispatch
                              [:poly/change-divisions {:divisions poly-den-div
                                                       :which     :denominator}]]
                             nil)
           merged-dispatch (into [] (remove nil? [num-dispatch den-dispatch tempo-dispatch]))]
       {:db (-> (:db cofx)
                (assoc-in [:polyrhythms :init?] true)
                (#(when (some? poly-verbose)
                    (assoc-in % [:polyrhythms :verbose?] poly-verbose))))
        :fx merged-dispatch}))))

(reg-event-fx :poly/change-divisions
              [standard-interceptors]
              (fn [cofx [_ new-values]]
                (let [{:keys [divisions which]} new-values
                      parsed-int    (max 1 (js/parseInt divisions))
                      db            (:db cofx)
                      temp          (assoc {:numerator   (-> db
                                                             :polyrhythms
                                                             :numerator
                                                             :divisions)
                                            :denominator (-> db
                                                             :polyrhythms
                                                             :denominator
                                                             :divisions)}
                                           which
                                           parsed-int)
                      lcm           (lcm (:numerator temp) (:denominator temp))
                      tempo         (-> db
                                        :polyrhythms
                                        :tempo)
                      storage-label (case which
                                      :numerator   "poly_num_div"
                                      :denominator "poly_den_div")]
                  (when (storage-available? "localStorage")
                    (-> js/window
                        .-localStorage
                        (.setItem storage-label (.stringify js/JSON parsed-int))))
                  {:db (-> db
                           (assoc-in [:polyrhythms which :divisions] parsed-int)
                           (assoc-in [:polyrhythms :lcm] lcm))
                   :fx [[:dispatch [:poly/change-last-beat-time (get-context-current-time)]]
                        [:dispatch
                         [:poly/update-input
                          [(-> ^js tempo
                               (.mul parsed-int)
                               (.toFraction true))
                           which]]]]})))

(reg-event-db :poly/inc-microbeat
              [standard-interceptors]
              (fn [db [_ which]] (update-in db [:polyrhythms which :microbeat] inc)))

(reg-event-db :poly/reset-microbeats
              [standard-interceptors]
              (fn [db [_ _]]
                (-> db
                    (assoc-in [:polyrhythms :numerator :microbeat] 0)
                    (assoc-in [:polyrhythms :denominator :microbeat] 0))))

(reg-event-db
 :poly/normalize-microbeats
 [standard-interceptors]
 (fn [db [_ _]]
   (let [{num-microbeat :microbeat num-divisions :divisions} (get-in db [:polyrhythms :numerator])
         {den-microbeat :microbeat den-divisions :divisions} (get-in db [:polyrhythms :denominator])
         tempo (get-in db [:polyrhythms :tempo])]
     (-> db
         (assoc-in [:polyrhythms :numerator :microbeat] (mod num-microbeat num-divisions))
         (assoc-in [:polyrhythms :denominator :microbeat] (mod den-microbeat den-divisions))
         (update-in [:polyrhythms :last-beat-time] + (get-seconds-per-beat (.valueOf tempo)))))))

(reg-event-db :poly/change-last-beat-time
              [check-spec-interceptor]
              (fn [db [_ new-value]] (assoc-in db [:polyrhythms :last-beat-time] new-value)))

(reg-event-fx
 :poly/check-diff
 [check-spec-interceptor]
 (fn [_cofx [_ args]]
   (if (> args 1) {:dispatch [:poly/change-last-beat-time (get-context-current-time)]} nil)))

(reg-event-fx :poly/set-tempo
              [check-spec-interceptor]
              (fn [cofx [_ tempo-input]]
                (try (let [tempo     (Fraction. tempo-input)
                           db        (:db cofx)
                           old-tempo (get-in db [:polyrhythms :tempo])
                           num-div   (get-in db [:polyrhythms :numerator :divisions])
                           den-div   (get-in db [:polyrhythms :denominator :divisions])
                           rectified (if (or (neg? (.-s ^js tempo))
                                             (zero? (.-n ^js tempo)))
                                       old-tempo
                                       tempo)
                           num-tempo (.mul ^js rectified num-div)
                           den-tempo (.mul ^js rectified den-div)]
                       {:db (assoc-in db [:polyrhythms :tempo] rectified)
                        :fx [[:dispatch
                              [:poly/check-diff
                               (-> rectified
                                   (.sub old-tempo)
                                   .abs
                                   .valueOf)]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction ^js rectified true)]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction num-tempo true) :numerator]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction den-tempo true) :denominator]]]]})
                     (catch :default _error nil))))

(reg-event-fx :poly/update-tempo
              [check-spec-interceptor]
              (fn [cofx [_ delta]]
                (let [tempo     (get-in cofx [:db :polyrhythms :tempo])
                      new-tempo (.add tempo delta)]
                  {:fx [[:dispatch [:poly/set-tempo new-tempo]]]})))

(reg-event-fx :poly/set-numerator-tempo
              [check-spec-interceptor]
              (fn [cofx [_ tempo-input]]
                (try (let [tempo      (Fraction. tempo-input)
                           db         (:db cofx)
                           old-tempo  (get-in db [:polyrhythms :tempo])
                           num-div    (get-in db [:polyrhythms :numerator :divisions])
                           den-div    (get-in db [:polyrhythms :denominator :divisions])
                           rectified  (if (or (neg? (.-s ^js tempo))
                                              (zero? (.-n ^js tempo)))
                                        old-tempo
                                        tempo)
                           main-tempo (.div ^js rectified num-div)
                           den-tempo  (.mul main-tempo den-div)]
                       {:db (assoc-in db [:polyrhythms :tempo] main-tempo)
                        :fx [[:dispatch
                              [:poly/check-diff
                               (-> main-tempo
                                   (.sub old-tempo)
                                   .abs
                                   .valueOf)]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction main-tempo true)]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction ^js rectified true) :numerator]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction den-tempo true) :denominator]]]]})
                     (catch :default _err nil))))

(reg-event-fx :poly/update-numerator-tempo
              [check-spec-interceptor]
              (fn [cofx [_ delta]]
                (let [num-string (get-in cofx [:db :polyrhythms :numerator :input])
                      num-tempo  (Fraction. num-string)
                      new-tempo  (.add num-tempo delta)]
                  {:fx [[:dispatch [:poly/set-numerator-tempo new-tempo]]]})))

(reg-event-fx :poly/set-denominator-tempo
              [check-spec-interceptor]
              (fn [cofx [_ tempo-input]]
                (try (let [tempo      (Fraction. tempo-input)
                           db         (:db cofx)
                           old-tempo  (get-in db [:polyrhythms :tempo])
                           num-div    (get-in db [:polyrhythms :numerator :divisions])
                           den-div    (get-in db [:polyrhythms :denominator :divisions])
                           rectified  (if (or (neg? (.-s ^js tempo))
                                              (zero? (.-n ^js tempo)))
                                        old-tempo
                                        tempo)
                           main-tempo (.div ^js rectified den-div)
                           num-tempo  (.mul main-tempo num-div)]
                       {:db (assoc-in db [:polyrhythms :tempo] main-tempo)
                        :fx [[:dispatch
                              [:poly/check-diff
                               (-> main-tempo
                                   (.sub old-tempo)
                                   .abs
                                   .valueOf)]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction main-tempo true)]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction num-tempo true) :numerator]]]
                             [:dispatch
                              [:poly/update-input
                               [(.toFraction ^js rectified true) :denominator]]]]})
                     (catch :default _err nil))))

(reg-event-fx :poly/update-denominator-tempo
              [check-spec-interceptor]
              (fn [cofx [_ delta]]
                (let [den-string (get-in cofx [:db :polyrhythms :denominator :input])
                      den-tempo  (Fraction. den-string)
                      new-tempo  (.add den-tempo delta)]
                  {:fx [[:dispatch [:poly/set-denominator-tempo new-tempo]]]})))

(reg-event-db :poly/toggle-playing
              [check-spec-interceptor]
              (fn [db [_]] (update-in db [:polyrhythms :playing?] not)))

(reg-event-db :poly/toggle-verbose?
              [check-spec-interceptor]
              (fn [db [_]]
                (when (storage-available? "localStorage")
                  (-> js/window
                      .-localStorage
                      (.setItem "poly_verbose"
                                (.stringify js/JSON (not (get-in db [:polyrhythms :verbose?]))))))
                (update-in db [:polyrhythms :verbose?] not)))

(reg-event-db
 :poly/update-input
 [check-spec-interceptor]
 (fn [db [_ [value type]]]
   (if (nil? type)
     (do
       (when (storage-available? "localStorage")
         (-> js/window
             .-localStorage
             (.setItem "poly_tempo" value)))
       (assoc-in db [:polyrhythms :tempo-input] value))
     (assoc-in db [:polyrhythms type :input] value))))

(reg-event-fx
 :poly/recalculate-grid-x
 [check-spec-interceptor]
 (fn [cofx [_]]
   (let [poly-db (get-in cofx [:db :polyrhythms])
         ref     (:cursor-ref poly-db)]
     (when (some? ref)
       (let [el-00-rec   (.getBoundingClientRect (js/document.getElementById "00"))
             start-x     (- (.-left el-00-rec) (/ CURSOR-WIDTH 2))
             width-x     (* (.-width el-00-rec) (get-in poly-db [:numerator :divisions]))
             grid-el-rec (.getBoundingClientRect (js/document.getElementById "grid"))
             height      (.-height grid-el-rec)
             start-y     (+ (.-top grid-el-rec) (.-scrollY js/window))]
         (js/console.log "rerender" start-x width-x)
         (set! (.. ref -style -left) (str start-x "px"))
         (set! (.. ref -style -top) (str start-y "px"))
         (.setAttribute ref "size" height)
         {:db (-> (:db cofx)
                  (assoc-in [:polyrhythms :grid-x] {:start start-x :width width-x}))})))))

(reg-event-fx :poly/cursor-ref
              [check-spec-interceptor]
              (fn [cofx [_ ref]]
                {:db (assoc-in (:db cofx) [:polyrhythms :cursor-ref] ref)
                 :fx [[:dispatch [:poly/recalculate-grid-x]]]}))

(reg-event-db :poly/set-control-select
              [check-spec-interceptor]
              (fn [db [_ type]]
                (assoc-in db [:polyrhythms :control-select] type)))