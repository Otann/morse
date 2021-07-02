(ns morse.updates.api-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]

            [morse.updates.api :as m-u-api]
            [morse.utils.test :as m-u-t]))

;; test data

(def token "fake-token")


;; test cases

(deftest get-updates-request
  (is (= #{"timeout=1" "offset=0" "limit=100"}
         (-> (m-u-api/get-updates token {})
             (m-u-t/capture-request-async)
             (m-u-t/extract-query-set))))

  (is (= #{"timeout=1" "offset=0" "limit=200"}
         (-> (m-u-api/get-updates token {:limit 200})
             (m-u-t/capture-request-async)
             (m-u-t/extract-query-set))))

  (is (= #{"timeout=1" "offset=31337" "limit=100"}
         (-> (m-u-api/get-updates token {:offset 31337})
             (m-u-t/capture-request-async)
             (m-u-t/extract-query-set))))

  (testing "method returns part of the response body"
    (let [updates {:foo "bar"}]
      (m-u-t/with-faked-updates
        updates

        (is (= updates (a/<!! (m-u-api/get-updates token {}))))))))
