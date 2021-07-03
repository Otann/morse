(ns morse.updates.api-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]

            [morse.updates.api :as updates.api]
            [morse.utils.test :as utils.test]))

;; test data

(def token "fake-token")

;; test cases

(deftest get-updates-request
  (is (= #{"timeout=1" "offset=0" "limit=100"}
         (-> (updates.api/get-updates token {})
             (utils.test/capture-request-async)
             (utils.test/extract-query-set))))

  (is (= #{"timeout=1" "offset=0" "limit=200"}
         (-> (updates.api/get-updates token {:limit 200})
             (utils.test/capture-request-async)
             (utils.test/extract-query-set))))

  (is (= #{"timeout=1" "offset=31337" "limit=100"}
         (-> (updates.api/get-updates token {:offset 31337})
             (utils.test/capture-request-async)
             (utils.test/extract-query-set))))

  (testing "method returns part of the response body"
    (let [updates {:foo "bar"}]
      (utils.test/with-faked-updates
        updates

        (is (= updates (a/<!! (updates.api/get-updates token {}))))))))
