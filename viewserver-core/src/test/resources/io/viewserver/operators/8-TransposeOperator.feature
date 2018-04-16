#Test is an invalid use of the operator
@ignore
Feature: Transpose operator fixture

  Background:
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
    Then operator "transpose1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | market | bucket1_notional | bucket2_notional | bucket1_id | bucket2_id |
      | SchemaReset |                  |             |       |        |                  |                  |            |            |
      | ColumnAdd   | bucket1_id       | Int         | 1     |        |                  |                  |            |            |
      | ColumnAdd   | bucket2_id       | Int         | 3     |        |                  |                  |            |            |
      | ColumnAdd   | market           | Int         | 0     |        |                  |                  |            |            |
      | ColumnAdd   | bucket1_notional | Long        | 2     |        |                  |                  |            |            |
      | ColumnAdd   | bucket2_notional | Long        | 4     |        |                  |                  |            |            |
      | DataReset   |                  |             |       |        |                  |                  |            |            |
      | RowAdd      |                  |             | 5     | 100    | 20               | 40               | 0          | 1          |
      | RowAdd      |                  |             | 6     | 30     | 60               | 0                | 2          | 0          |
      | RowAdd      |                  |             | 7     | 40     | 60               | 60               | 4          | 5          |
    And commit

