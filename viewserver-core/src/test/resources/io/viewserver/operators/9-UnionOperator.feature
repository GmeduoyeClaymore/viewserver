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
    Then operator "union1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | bucket | id | market | notional |
      | SchemaReset |          |             |       |        |    |        |          |
      | ColumnAdd   | id       | Int         | 0     |        |    |        |          |
      | ColumnAdd   | market   | Int         | 1     |        |    |        |          |
      | ColumnAdd   | bucket   | Int         | 2     |        |    |        |          |
      | ColumnAdd   | notional | Long        | 3     |        |    |        |          |
      | ColumnAdd   | sourceId | Int         | 4     |        |    |        |          |
      | DataReset   |          |             |       |        |    |        |          |
      | RowAdd      |          |             | 0     | 1      | 0  | 100    | 20       |
      | RowAdd      |          |             | 1     | 2      | 1  | 100    | 40       |
      | RowAdd      |          |             | 2     | 1      | 2  | 30     | 60       |
      | RowAdd      |          |             | 3     | 2      | 3  | 40     | 60       |
      | RowAdd      |          |             | 4     | 1      | 4  | 40     | 60       |
      | RowAdd      |          |             | 5     | 2      | 5  | 40     | 60       |
      | RowAdd      |          |             | 6     | 1      | 6  | 100    | 20       |
      | RowAdd      |          |             | 7     | 2      | 7  | 100    | 40       |
      | RowAdd      |          |             | 8     | 1      | 8  | 30     | 60       |
      | RowAdd      |          |             | 9     | 2      | 9  | 40     | 60       |
      | RowAdd      |          |             | 10    | 1      | 10 | 40     | 60       |
      | RowAdd      |          |             | 11    | 2      | 11 | 40     | 60       |
    And commit

  Scenario: Unioned table updates when one source table values are updated
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    Then operator "union1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | bucket | id | market | notional |
      | SchemaReset |          |             |       |        |    |        |          |
      | ColumnAdd   | id       | Int         | 0     |        |    |        |          |
      | ColumnAdd   | market   | Int         | 1     |        |    |        |          |
      | ColumnAdd   | bucket   | Int         | 2     |        |    |        |          |
      | ColumnAdd   | notional | Long        | 3     |        |    |        |          |
      | ColumnAdd   | sourceId | Int         | 4     |        |    |        |          |
      | DataReset   |          |             |       |        |    |        |          |
      | RowAdd      |          |             | 0     | 1      | 0  | 100    | 20       |
      | RowAdd      |          |             | 1     | 2      | 1  | 100    | 40       |
      | RowAdd      |          |             | 2     | 1      | 2  | 30     | 60       |
      | RowAdd      |          |             | 3     | 2      | 3  | 40     | 60       |
      | RowAdd      |          |             | 4     | 1      | 4  | 40     | 60       |
      | RowAdd      |          |             | 5     | 2      | 5  | 40     | 60       |
      | RowAdd      |          |             | 6     | 1      | 6  | 100    | 20       |
      | RowAdd      |          |             | 7     | 2      | 7  | 100    | 40       |
      | RowAdd      |          |             | 8     | 1      | 8  | 30     | 60       |
      | RowAdd      |          |             | 9     | 2      | 9  | 40     | 60       |
      | RowAdd      |          |             | 10    | 1      | 10 | 40     | 60       |
      | RowAdd      |          |             | 11    | 2      | 11 | 40     | 60       |
    And commit
    When table "source" updated to
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 0      | 1000       | 10         | 200           |
      | 1      | 1000       | 20         | 400           |
      | 2      | 300        | 10         | 600           |
      | 3      | 400        | 20         | 600           |
      | 4      | 400        | 10         | 600           |
      | 5      | 400        | 20         | 600           |
    Then operator "union1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | bucket | id | market | notional |
      | RowUpdate |       |             | 0     | 10     | 0  | 1000   | 200      |
      | RowUpdate |       |             | 1     | 20     | 1  | 1000   | 400      |
      | RowUpdate |       |             | 2     | 10     | 2  | 300    | 600      |
      | RowUpdate |       |             | 3     | 20     | 3  | 400    | 600      |
      | RowUpdate |       |             | 4     | 10     | 4  | 400    | 600      |
      | RowUpdate |       |             | 5     | 20     | 5  | 400    | 600      |
    And commit

  Scenario: Unioned table is updated when source table values are added
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    Then operator "union1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | bucket | id | market | notional |
      | SchemaReset |          |             |       |        |    |        |          |
      | ColumnAdd   | id       | Int         | 0     |        |    |        |          |
      | ColumnAdd   | market   | Int         | 1     |        |    |        |          |
      | ColumnAdd   | bucket   | Int         | 2     |        |    |        |          |
      | ColumnAdd   | notional | Long        | 3     |        |    |        |          |
      | ColumnAdd   | sourceId | Int         | 4     |        |    |        |          |
      | DataReset   |          |             |       |        |    |        |          |
      | RowAdd      |          |             | 0     | 1      | 0  | 100    | 20       |
      | RowAdd      |          |             | 1     | 2      | 1  | 100    | 40       |
      | RowAdd      |          |             | 2     | 1      | 2  | 30     | 60       |
      | RowAdd      |          |             | 3     | 2      | 3  | 40     | 60       |
      | RowAdd      |          |             | 4     | 1      | 4  | 40     | 60       |
      | RowAdd      |          |             | 5     | 2      | 5  | 40     | 60       |
      | RowAdd      |          |             | 6     | 1      | 6  | 100    | 20       |
      | RowAdd      |          |             | 7     | 2      | 7  | 100    | 40       |
      | RowAdd      |          |             | 8     | 1      | 8  | 30     | 60       |
      | RowAdd      |          |             | 9     | 2      | 9  | 40     | 60       |
      | RowAdd      |          |             | 10    | 1      | 10 | 40     | 60       |
      | RowAdd      |          |             | 11    | 2      | 11 | 40     | 60       |
    And commit
    When table "source" updated to
      | id~Int | market~Int | bucket~Int | notional~Long |
      | 100    | 1000       | 10         | 200           |
      | 200    | 1000       | 20         | 400           |
      | 300    | 300        | 10         | 600           |
      | 400    | 400        | 20         | 600           |
      | 500    | 400        | 10         | 600           |
      | 600    | 400        | 20         | 600           |
    Then operator "union1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | bucket | id  | market | notional |
      | RowAdd  |       |             | 12    | 10     | 100 | 1000   | 200      |
      | RowAdd  |       |             | 13    | 20     | 200 | 1000   | 400      |
      | RowAdd  |       |             | 14    | 10     | 300 | 300    | 600      |
      | RowAdd  |       |             | 15    | 20     | 400 | 400    | 600      |
      | RowAdd  |       |             | 16    | 10     | 500 | 400    | 600      |
      | RowAdd  |       |             | 17    | 20     | 600 | 400    | 600      |
    And commit

    #How do I just get the latest view of the grid?
  Scenario: Remove column from source
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    Then operator "union1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | bucket | id | market | notional |
      | SchemaReset |          |             |       |        |    |        |          |
      | ColumnAdd   | id       | Int         | 0     |        |    |        |          |
      | ColumnAdd   | market   | Int         | 1     |        |    |        |          |
      | ColumnAdd   | bucket   | Int         | 2     |        |    |        |          |
      | ColumnAdd   | notional | Long        | 3     |        |    |        |          |
      | ColumnAdd   | sourceId | Int         | 4     |        |    |        |          |
      | DataReset   |          |             |       |        |    |        |          |
      | RowAdd      |          |             | 0     | 1      | 0  | 100    | 20       |
      | RowAdd      |          |             | 1     | 2      | 1  | 100    | 40       |
      | RowAdd      |          |             | 2     | 1      | 2  | 30     | 60       |
      | RowAdd      |          |             | 3     | 2      | 3  | 40     | 60       |
      | RowAdd      |          |             | 4     | 1      | 4  | 40     | 60       |
      | RowAdd      |          |             | 5     | 2      | 5  | 40     | 60       |
      | RowAdd      |          |             | 6     | 1      | 6  | 100    | 20       |
      | RowAdd      |          |             | 7     | 2      | 7  | 100    | 40       |
      | RowAdd      |          |             | 8     | 1      | 8  | 30     | 60       |
      | RowAdd      |          |             | 9     | 2      | 9  | 40     | 60       |
      | RowAdd      |          |             | 10    | 1      | 10 | 40     | 60       |
      | RowAdd      |          |             | 11    | 2      | 11 | 40     | 60       |
    And commit
    When columns "market" removed from table "source"
    Then operator "union1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | bucket | id | market | notional |
    And commit

    #???????????????????????????????????????????????????????
  Scenario: Column added to unioned tabed when added to source
    When operator type "union" named "union1" created
      | field | value |
    And operator "source" output "out" plugged into "union1" input "in1~1"
    And operator "source2" output "out" plugged into "union1" input "in2~2"
    Then operator "union1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | bucket | id | market | notional |
      | SchemaReset |          |             |       |        |    |        |          |
      | ColumnAdd   | id       | Int         | 0     |        |    |        |          |
      | ColumnAdd   | market   | Int         | 1     |        |    |        |          |
      | ColumnAdd   | bucket   | Int         | 2     |        |    |        |          |
      | ColumnAdd   | notional | Long        | 3     |        |    |        |          |
      | ColumnAdd   | sourceId | Int         | 4     |        |    |        |          |
      | DataReset   |          |             |       |        |    |        |          |
      | RowAdd      |          |             | 0     | 1      | 0  | 100    | 20       |
      | RowAdd      |          |             | 1     | 2      | 1  | 100    | 40       |
      | RowAdd      |          |             | 2     | 1      | 2  | 30     | 60       |
      | RowAdd      |          |             | 3     | 2      | 3  | 40     | 60       |
      | RowAdd      |          |             | 4     | 1      | 4  | 40     | 60       |
      | RowAdd      |          |             | 5     | 2      | 5  | 40     | 60       |
      | RowAdd      |          |             | 6     | 1      | 6  | 100    | 20       |
      | RowAdd      |          |             | 7     | 2      | 7  | 100    | 40       |
      | RowAdd      |          |             | 8     | 1      | 8  | 30     | 60       |
      | RowAdd      |          |             | 9     | 2      | 9  | 40     | 60       |
      | RowAdd      |          |             | 10    | 1      | 10 | 40     | 60       |
      | RowAdd      |          |             | 11    | 2      | 11 | 40     | 60       |
    And commit
    When columns added to table "source"
      | ~Name | ~ColumnType |
      | col1  | String      |
      | col2  | Long        |
    Then operator "union1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | bucket | id | market | notional |
    And commit
