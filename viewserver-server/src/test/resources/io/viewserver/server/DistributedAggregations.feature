Feature: Distributed Aggregations

  Background:
    Given an in-process viewserver with 2 slave nodes
    And a data source defined by "distributed_aggregations_datasource.json"
    And a report defined by "distributed_aggregations_report.json"
    And a client connected to "inproc://master"
    And the following records in table "distributed_aggregations"
      | customer | value | include |
      | One      | 1000  | true    |
      | One      | 2000  | false   |

  Scenario: Sum
    Given report parameters
      | Name     | Type   | Value |
      | measures | String | sum   |
    When I subscribe to report "distributed_aggregations_report"
    Then the following data is received
      | customer | value_sum |
      | One      | 3000      |

  Scenario: Average
    Given report parameters
      | Name     | Type   | Value   |
      | measures | String | average |
    When I subscribe to report "distributed_aggregations_report"
    Then the following data is received
      | customer | value_avg |
      | One      | 1500      |

  Scenario: Average with custom count column
    Given report parameters
      | Name     | Type   | Value            |
      | measures | String | average_included |
    When I subscribe to report "distributed_aggregations_report"
    Then the following data is received
      | customer | value_avg_included |
      | One      | 1000               |
