Feature: Placeholder

  Background: Load the test data
    Given the following dataset
      | col1 | col2 | col3 |
      | 111  | aaa  | bbb  |
      | 222  | ccc  | ddd  |
      | 333  | eee  | fff  |

  Scenario: Open a report
    When the report 'Report x' is selected
    And the report is aggregated by 'ABC'
    Then the generated report is
      | key | metric1 | metric2 | metric3 |
      | 1   | 123     | 123     | 123     |
      | 2   | 234     | 234     | 234     |

