Feature: Index operator fixture

  Background:
    Given table named "source" with data
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 3       | 60            |
      | 3      | 40         | 3       | 60            |


  Scenario: Can index a single column with multiple values
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
    And operator "source" output "out" plugged into "index1" input "in"
    Then operator "index1" output "market~10,40" is
      | ~Action     | ~Name    | ~TEId | ~ColumnType | market | notional | day |
      | SchemaReset |          |       |             |        |          |     |
      | ColumnAdd   | id       | 0     | Int         |        |          |     |
      | ColumnAdd   | market   | 1     | Int         |        |          |     |
      | ColumnAdd   | day      | 2     | Int         |        |          |     |
      | ColumnAdd   | notional | 3     | Long        |        |          |     |
      | DataReset   |          |       |             |        |          |     |
      | RowAdd      | null     | 0     | 0           | 10     | 20       | 1   |
      | RowAdd      | null     | 1     | 1           | 10     | 40       | 2   |
      | RowAdd      | null     | 2     | 2           | 40     | 60       | 3   |
    And commit

  Scenario: Can update index to change output
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
    And operator "source" output "out" plugged into "index1" input "in"
    Then operator "index1" output "market~10,30" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | day | market | notional |
      | SchemaReset |          |             |       |     |        |          |
      | ColumnAdd   | id       | Int         | 0     |     |        |          |
      | ColumnAdd   | market   | Int         | 1     |     |        |          |
      | ColumnAdd   | day      | Int         | 2     |     |        |          |
      | ColumnAdd   | notional | Long        | 3     |     |        |          |
      | DataReset   |          |             |       |     |        |          |
      | RowAdd      |          |             | 0     | 1   | 10     | 20       |
      | RowAdd      |          |             | 1     | 2   | 10     | 40       |
      | RowAdd      |          |             | 2     | 3   | 30     | 60       |
    And commit

  Scenario: Can index a multiple columns with multiple values
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
    Then operator "index1" output "market~10,40#day~2" is
      | ~Action     | ~Name    | ~TEId | ~ColumnType | market | notional | day |
      | SchemaReset |          |       |             |        |          |     |
      | ColumnAdd   | id       | 0     | Int         |        |          |     |
      | ColumnAdd   | market   | 1     | Int         |        |          |     |
      | ColumnAdd   | day      | 2     | Int         |        |          |     |
      | ColumnAdd   | notional | 3     | Long        |        |          |     |
      | DataReset   |          |       |             |        |          |     |
      | RowAdd      | null     | 0     | 1           | 10     | 40       | 2   |
    And commit

  Scenario: Updated rows reflected in indexed table output
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
    Then operator "index1" output "market~10,40#day~2" is
      | ~Action     | ~Name    | ~TEId | ~ColumnType | market | notional | day |
      | SchemaReset |          |       |             |        |          |     |
      | ColumnAdd   | id       | 0     | Int         |        |          |     |
      | ColumnAdd   | market   | 1     | Int         |        |          |     |
      | ColumnAdd   | day      | 2     | Int         |        |          |     |
      | ColumnAdd   | notional | 3     | Long        |        |          |     |
      | DataReset   |          |       |             |        |          |     |
      | RowAdd      | null     | 0     | 1           | 10     | 40       | 2   |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 2       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 2       | 60            |
      | 3      | 40         | 2       | 60            |
    Then operator "index1" output "market~10,40#day~2" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | day | market | notional |
      | RowAdd    |       |             | 1     | 2   | 10     | 20       |
      | RowUpdate |       |             | 0     | 2   | 10     | 40       |
      | RowAdd    |       |             | 2     | 2   | 40     | 60       |
    And commit

  Scenario: New rows added to source reflected in indexed table output
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
    Then operator "index1" output "market~10,40#day~2" is
      | ~Action     | ~Name    | ~TEId | ~ColumnType | market | notional | day |
      | SchemaReset |          |       |             |        |          |     |
      | ColumnAdd   | id       | 0     | Int         |        |          |     |
      | ColumnAdd   | market   | 1     | Int         |        |          |     |
      | ColumnAdd   | day      | 2     | Int         |        |          |     |
      | ColumnAdd   | notional | 3     | Long        |        |          |     |
      | DataReset   |          |       |             |        |          |     |
      | RowAdd      | null     | 0     | 1           | 10     | 40       | 2   |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 4      | 10         | 1       | 20            |
      | 5      | 10         | 2       | 40            |
      | 6      | 30         | 3       | 60            |
      | 7      | 40         | 2       | 60            |
    Then operator "index1" output "market~10,40#day~2" is
      | ~Action | ~Name | ~ColumnType | ~TEId | day | market | notional |
      | RowAdd  |       |             | 1     | 2   | 10     | 40       |
      | RowAdd  |       |             | 2     | 2   | 40     | 60       |
    And commit


