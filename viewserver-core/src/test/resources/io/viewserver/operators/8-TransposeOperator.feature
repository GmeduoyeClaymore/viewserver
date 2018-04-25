#Test is an invalid use of the operator
@ignore
Feature: Transpose operator fixture

  Background:
    Given id field is "market"
    Given table named "source" with data
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 0      | 100        | 1          | 20            |
      | 1      | 100        | 2          | 40            |
      | 2      | 30         | 1          | 60            |
      | 3      | 40         | 2          | 60            |
      | 4      | 40         | 1          | 60            |
      | 5      | 40         | 2          | 60            |


  Scenario: Can transpose by single column
    When operator type "transpose" named "transpose1" created
      | field        | value  |
      | keyColumns   | market |
      | pivotColumns | bucket |
      | pivotValues  | 1,2    |
    And operator "source" output "out" plugged into "transpose1" input "in"
    And commit
    Then schema for "transpose1" is
      | ~Action     | ~Name            | ~ColumnType |
      | ColumnAdd   | bucket1_id       | Int         |
      | ColumnAdd   | bucket2_id       | Int         |
      | ColumnAdd   | market           | Int         |
      | ColumnAdd   | bucket1_notional | Long        |
      | ColumnAdd   | bucket2_notional | Long        |
    Then data for "transpose1" is
      | ~Action     |  market | bucket1_notional | bucket2_notional | bucket1_id | bucket2_id |
      | RowAdd      |  100    | 20               | 40               | 0          | 1          |
      | RowAdd      |  30     | 60               | 0                | 2          | 0          |
      | RowAdd      |  40     | 60               | 60               | 4          | 5          |

