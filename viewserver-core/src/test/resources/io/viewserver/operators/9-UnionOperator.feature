Feature: Union operator fixture

  Background:
    Given table named "source" with data
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 0      | 100        | 1          | 20            |
      | 1      | 100        | 2          | 40            |
      | 2      | 30         | 1          | 60            |
      | 3      | 40         | 2          | 60            |
      | 4      | 40         | 1          | 60            |
      | 5      | 40         | 2          | 60            |
    Given table named "source2" with data
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 6      | 100        | 1          | 20            |
      | 7      | 100        | 2          | 40            |
      | 8      | 30         | 1          | 60            |
      | 9      | 40         | 2          | 60            |
      | 10     | 40         | 1          | 60            |
      | 11     | 40         | 2          | 60            |


  Scenario: Can union two tables
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    And commit
    Then schema for "union1" is
      | ~Action     | ~Name    | ~ColumnType |
      | ColumnAdd   | id       | Int         |
      | ColumnAdd   | market   | Int         |
      | ColumnAdd   | bucket   | Int         |
      | ColumnAdd   | notional | Long        |
      | ColumnAdd   | sourceId | Int         |
    Then data for "union1" is
      | ~Action     | bucket | id | market | notional |
      | RowAdd      | 1      | 0  | 100    | 20       |
      | RowAdd      | 2      | 1  | 100    | 40       |
      | RowAdd      | 1      | 2  | 30     | 60       |
      | RowAdd      | 2      | 3  | 40     | 60       |
      | RowAdd      | 1      | 4  | 40     | 60       |
      | RowAdd      | 2      | 5  | 40     | 60       |
      | RowAdd      | 1      | 6  | 100    | 20       |
      | RowAdd      | 2      | 7  | 100    | 40       |
      | RowAdd      | 1      | 8  | 30     | 60       |
      | RowAdd      | 2      | 9  | 40     | 60       |
      | RowAdd      | 1      | 10 | 40     | 60       |
      | RowAdd      | 2      | 11 | 40     | 60       |

  Scenario: Unioned table updates when one source table values are updated
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    And commit
    Then data for "union1" is
      | ~Action     | bucket | id | market | notional |
      | RowAdd      | 1      | 0  | 100    | 20       |
      | RowAdd      | 2      | 1  | 100    | 40       |
      | RowAdd      | 1      | 2  | 30     | 60       |
      | RowAdd      | 2      | 3  | 40     | 60       |
      | RowAdd      | 1      | 4  | 40     | 60       |
      | RowAdd      | 2      | 5  | 40     | 60       |
      | RowAdd      | 1      | 6  | 100    | 20       |
      | RowAdd      | 2      | 7  | 100    | 40       |
      | RowAdd      | 1      | 8  | 30     | 60       |
      | RowAdd      | 2      | 9  | 40     | 60       |
      | RowAdd      | 1      | 10 | 40     | 60       |
      | RowAdd      | 2      | 11 | 40     | 60       |
    When table "source" updated to
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 0      | 1000       | 10         | 200           |
      | 1      | 1000       | 20         | 400           |
      | 2      | 300        | 10         | 600           |
      | 3      | 400        | 20         | 600           |
      | 4      | 400        | 10         | 600           |
      | 5      | 400        | 20         | 600           |
    And commit
    Then data for "union1" is
      | ~Action   | bucket | id | market | notional |
      | RowUpdate | 10     | 0  | 1000   | 200      |
      | RowUpdate | 20     | 1  | 1000   | 400      |
      | RowUpdate | 10     | 2  | 300    | 600      |
      | RowUpdate | 20     | 3  | 400    | 600      |
      | RowUpdate | 10     | 4  | 400    | 600      |
      | RowUpdate | 20     | 5  | 400    | 600      |

  Scenario: Unioned table is updated when source table values are added
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    And commit
    Then data for "union1" is
      | ~Action     | bucket | id | market | notional |
      | RowAdd      | 1      | 0  | 100    | 20       |
      | RowAdd      | 2      | 1  | 100    | 40       |
      | RowAdd      | 1      | 2  | 30     | 60       |
      | RowAdd      | 2      | 3  | 40     | 60       |
      | RowAdd      | 1      | 4  | 40     | 60       |
      | RowAdd      | 2      | 5  | 40     | 60       |
      | RowAdd      | 1      | 6  | 100    | 20       |
      | RowAdd      | 2      | 7  | 100    | 40       |
      | RowAdd      | 1      | 8  | 30     | 60       |
      | RowAdd      | 2      | 9  | 40     | 60       |
      | RowAdd      | 1      | 10 | 40     | 60       |
      | RowAdd      | 2      | 11 | 40     | 60       |
    When table "source" updated to
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 100    | 1000       | 10         | 200           |
      | 200    | 1000       | 20         | 400           |
      | 300    | 300        | 10         | 600           |
      | 400    | 400        | 20         | 600           |
      | 500    | 400        | 10         | 600           |
      | 600    | 400        | 20         | 600           |
    And commit
    Then data for "union1" is
      | ~Action | bucket | id  | market | notional |
      | RowAdd  | 10     | 100 | 1000   | 200      |
      | RowAdd  | 20     | 200 | 1000   | 400      |
      | RowAdd  | 10     | 300 | 300    | 600      |
      | RowAdd  | 20     | 400 | 400    | 600      |
      | RowAdd  | 10     | 500 | 400    | 600      |
      | RowAdd  | 20     | 600 | 400    | 600      |
    And commit

    #How do I just get the latest view of the grid?
  Scenario: Remove column from source
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    And commit
    Then data for "union1" is
      | ~Action     | bucket | id | market | notional |
      | RowAdd      | 1      | 0  | 100    | 20       |
      | RowAdd      | 2      | 1  | 100    | 40       |
      | RowAdd      | 1      | 2  | 30     | 60       |
      | RowAdd      | 2      | 3  | 40     | 60       |
      | RowAdd      | 1      | 4  | 40     | 60       |
      | RowAdd      | 2      | 5  | 40     | 60       |
      | RowAdd      | 1      | 6  | 100    | 20       |
      | RowAdd      | 2      | 7  | 100    | 40       |
      | RowAdd      | 1      | 8  | 30     | 60       |
      | RowAdd      | 2      | 9  | 40     | 60       |
      | RowAdd      | 1      | 10 | 40     | 60       |
      | RowAdd      | 2      | 11 | 40     | 60       |
    When columns "market" removed from table "source"
    And commit
    Then data for "union1" is
      | ~Action | ~Name | ~ColumnType | ~TEId | bucket | id | market | notional |

    #???????????????????????????????????????????????????????
  Scenario: Column added to unioned tabed when added to source
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    And commit
    Then data for "union1" is
      | ~Action     | bucket | id | market | notional |
      | RowAdd      | 1      | 0  | 100    | 20       |
      | RowAdd      | 2      | 1  | 100    | 40       |
      | RowAdd      | 1      | 2  | 30     | 60       |
      | RowAdd      | 2      | 3  | 40     | 60       |
      | RowAdd      | 1      | 4  | 40     | 60       |
      | RowAdd      | 2      | 5  | 40     | 60       |
      | RowAdd      | 1      | 6  | 100    | 20       |
      | RowAdd      | 2      | 7  | 100    | 40       |
      | RowAdd      | 1      | 8  | 30     | 60       |
      | RowAdd      | 2      | 9  | 40     | 60       |
      | RowAdd      | 1      | 10 | 40     | 60       |
      | RowAdd      | 2      | 11 | 40     | 60       |
    When columns added to table "source"
      | ~Name | ~ColumnType |
      | col1  | String      |
      | col2  | Long        |
    And commit
    Then data for "union1" is
      | ~Action | ~Name | ~ColumnType | ~TEId | bucket | id | market | notional |
