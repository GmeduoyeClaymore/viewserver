Feature: Join operator fixture

  Background:
    Given table named "source1" with data
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 3       | 60            |
      | 3      | 40         | 3       | 60            |
    And table named "source2" with data
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 25            |
      | 1      | 13         | 2       | 45            |
      | 2      | 33         | 3       | 65            |
      | 3      | 45         | 3       | 65            |


  Scenario: Can join two tables
    When operator type "join" named "join1" created
      | field       | value  |
      | leftColumn  | market |
      | rightColumn | market |
    And operator "source1" output "out" plugged into "join1" input "left"
    And operator "source2" output "out" plugged into "join1" input "right"
    Then operator "join1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
      | SchemaReset |                  |             |       |    |        |          |            |             |                  |                |
      | ColumnAdd   | id               | Int         | 0     |    |        |          |            |             |                  |                |
      | ColumnAdd   | market           | Int         | 1     |    |        |          |            |             |                  |                |
      | ColumnAdd   | day              | Int         | 2     |    |        |          |            |             |                  |                |
      | ColumnAdd   | notional         | Long        | 3     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_id       | Int         | 4     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_market   | Int         | 5     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_day      | Int         | 6     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_notional | Long        | 7     |    |        |          |            |             |                  |                |
      | DataReset   |                  |             |       |    |        |          |            |             |                  |                |
      | RowAdd      | null             | null        | 0     | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
      | RowAdd      | null             | null        | 1     | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
    And commit

  Scenario: Updated rows appear in joined table
    When operator type "join" named "join1" created
      | field       | value  |
      | leftColumn  | market |
      | rightColumn | market |
    And operator "source1" output "out" plugged into "join1" input "left"
    And operator "source2" output "out" plugged into "join1" input "right"
    Then operator "join1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
      | SchemaReset |                  |             |       |    |        |          |            |             |                  |                |
      | ColumnAdd   | id               | Int         | 0     |    |        |          |            |             |                  |                |
      | ColumnAdd   | market           | Int         | 1     |    |        |          |            |             |                  |                |
      | ColumnAdd   | day              | Int         | 2     |    |        |          |            |             |                  |                |
      | ColumnAdd   | notional         | Long        | 3     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_id       | Int         | 4     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_market   | Int         | 5     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_day      | Int         | 6     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_notional | Long        | 7     |    |        |          |            |             |                  |                |
      | DataReset   |                  |             |       |    |        |          |            |             |                  |                |
      | RowAdd      | null             | null        | 0     | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
      | RowAdd      | null             | null        | 1     | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
    And commit
    When table "source1" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 10         | 3       | 60            |
      | 3      | 10         | 3       | 60            |
    Then operator "join1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
      | RowUpdate | null  | null        | 1     | 0  | 10     | 20       | 1           | 0          | 10             | 25               |
      | RowUpdate | null  | null        | 0     | 1  | 10     | 40       | 1           | 0          | 10             | 25               |
      | RowAdd    | null  | null        | 2     | 2  | 10     | 60       | 1           | 0          | 10             | 25               |
      | RowAdd    | null  | null        | 3     | 3  | 10     | 60       | 1           | 0          | 10             | 25               |
    And commit

  Scenario: Joined table updated when row in source is deleted
    When operator type "join" named "join1" created
      | field       | value  |
      | leftColumn  | market |
      | rightColumn | market |
    And operator "source1" output "out" plugged into "join1" input "left"
    And operator "source2" output "out" plugged into "join1" input "right"
    Then operator "join1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
      | SchemaReset |                  |             |       |    |        |          |            |             |                  |                |
      | ColumnAdd   | id               | Int         | 0     |    |        |          |            |             |                  |                |
      | ColumnAdd   | market           | Int         | 1     |    |        |          |            |             |                  |                |
      | ColumnAdd   | day              | Int         | 2     |    |        |          |            |             |                  |                |
      | ColumnAdd   | notional         | Long        | 3     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_id       | Int         | 4     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_market   | Int         | 5     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_day      | Int         | 6     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_notional | Long        | 7     |    |        |          |            |             |                  |                |
      | DataReset   |                  |             |       |    |        |          |            |             |                  |                |
      | RowAdd      | null             | null        | 0     | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
      | RowAdd      | null             | null        | 1     | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
    And commit
    When rows "1,2" removed from table "source1"
    Then operator "join1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
      | RowRemove |       |             | 0     |    |        |          |             |            |                |                  |
    And commit


  Scenario: Newly inserted rows appear in joined table
    When operator type "join" named "join1" created
      | field       | value  |
      | leftColumn  | market |
      | rightColumn | market |
    And operator "source1" output "out" plugged into "join1" input "left"
    And operator "source2" output "out" plugged into "join1" input "right"
    Then operator "join1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
      | SchemaReset |                  |             |       |    |        |          |            |             |                  |                |
      | ColumnAdd   | id               | Int         | 0     |    |        |          |            |             |                  |                |
      | ColumnAdd   | market           | Int         | 1     |    |        |          |            |             |                  |                |
      | ColumnAdd   | day              | Int         | 2     |    |        |          |            |             |                  |                |
      | ColumnAdd   | notional         | Long        | 3     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_id       | Int         | 4     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_market   | Int         | 5     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_day      | Int         | 6     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_notional | Long        | 7     |    |        |          |            |             |                  |                |
      | DataReset   |                  |             |       |    |        |          |            |             |                  |                |
      | RowAdd      | null             | null        | 0     | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
      | RowAdd      | null             | null        | 1     | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
    And commit
    When table "source1" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 4      | 10         | 1       | 20            |
      | 5      | 10         | 2       | 40            |
      | 6      | 10         | 3       | 60            |
      | 7      | 10         | 3       | 60            |
    Then operator "join1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
      | RowAdd  | null  | null        | 2     | 4  | 10     | 20       | 1           | 0          | 10 << 10       | 25               |
      | RowAdd  | null  | null        | 3     | 5  | 10     | 40       | 1           | 0          | 10 << 10       | 25               |
      | RowAdd  | null  | null        | 4     | 6  | 10     | 60       | 1           | 0          | 10 << 10       | 25               |
      | RowAdd  | null  | null        | 5     | 7  | 10     | 60       | 1           | 0          | 10 << 10       | 25               |
    And commit

  #???????????????????????????????????????????????????????????
  Scenario: Joined table updated when column is removed
    When operator type "join" named "join1" created
      | field       | value  |
      | leftColumn  | market |
      | rightColumn | market |
    And operator "source1" output "out" plugged into "join1" input "left"
    And operator "source2" output "out" plugged into "join1" input "right"
    Then operator "join1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
      | SchemaReset |                  |             |       |    |        |          |            |             |                  |                |
      | ColumnAdd   | id               | Int         | 0     |    |        |          |            |             |                  |                |
      | ColumnAdd   | market           | Int         | 1     |    |        |          |            |             |                  |                |
      | ColumnAdd   | day              | Int         | 2     |    |        |          |            |             |                  |                |
      | ColumnAdd   | notional         | Long        | 3     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_id       | Int         | 4     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_market   | Int         | 5     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_day      | Int         | 6     |    |        |          |            |             |                  |                |
      | ColumnAdd   | source2_notional | Long        | 7     |    |        |          |            |             |                  |                |
      | DataReset   |                  |             |       |    |        |          |            |             |                  |                |
      | RowAdd      | null             | null        | 0     | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
      | RowAdd      | null             | null        | 1     | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
    And commit
    When columns "day" removed from table "source1"
    When columns "day" removed from table "source2"
    Then operator "join1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | id | market | notional | source2_day | source2_id | source2_market | source2_notional |

    And commit