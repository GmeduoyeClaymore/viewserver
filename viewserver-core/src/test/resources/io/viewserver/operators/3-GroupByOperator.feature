Feature: GroupBy operator fixture

  Background:
    Given table named "source" with data
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 3       | 60            |
      | 3      | 40         | 3       | 60            |


  Scenario: Can group by single column
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | notionalSum |
      | SchemaReset |             |       |             |        |             |
      | ColumnAdd   | market      | 0     | Int         |        |             |
      | ColumnAdd   | count       | 1     | Int         |        |             |
      | ColumnAdd   | notionalSum | 2     | Long        |        |             |
      | DataReset   |             |       |             |        |             |
      | RowAdd      |             | 0     |             | 10     | 60          |
      | RowAdd      |             | 1     |             | 30     | 60          |
      | RowAdd      |             | 2     |             | 40     | 60          |
    And commit

  Scenario: Edited source is reflected in group by table
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | notionalSum |
      | SchemaReset |             |       |             |        |             |
      | ColumnAdd   | market      | 0     | Int         |        |             |
      | ColumnAdd   | count       | 1     | Int         |        |             |
      | ColumnAdd   | notionalSum | 2     | Long        |        |             |
      | DataReset   |             |       |             |        |             |
      | RowAdd      |             | 0     |             | 10     | 60          |
      | RowAdd      |             | 1     |             | 30     | 60          |
      | RowAdd      |             | 2     |             | 40     | 60          |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 60            |
      | 1      | 30         | 2       | 40            |
      | 2      | 30         | 3       | 60            |
      | 3      | 40         | 3       | 60            |
    Then operator "groupBy1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | market | notionalSum |
      | RowUpdate |       |             | 0     | 10     | 60          |
      | RowUpdate |       |             | 1     | 30     | 100         |
    And commit

  Scenario: New rows added to source reflected in group by table
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | notionalSum |
      | SchemaReset |             |       |             |        |             |
      | ColumnAdd   | market      | 0     | Int         |        |             |
      | ColumnAdd   | count       | 1     | Int         |        |             |
      | ColumnAdd   | notionalSum | 2     | Long        |        |             |
      | DataReset   |             |       |             |        |             |
      | RowAdd      |             | 0     |             | 10     | 60          |
      | RowAdd      |             | 1     |             | 30     | 60          |
      | RowAdd      |             | 2     |             | 40     | 60          |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 4      | 10         | 1       | 60            |
      | 5      | 30         | 2       | 40            |
      | 6      | 30         | 3       | 60            |
      | 7      | 40         | 3       | 60            |
    Then operator "groupBy1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | market | notionalSum |
      | RowUpdate |       |             | 0     | 10     | 120         |
      | RowUpdate |       |             | 1     | 30     | 160         |
      | RowUpdate |       |             | 2     | 40     | 120         |
    And commit

  Scenario: Rows deleted from source reflected in group by table
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | notionalSum |
      | SchemaReset |             |       |             |        |             |
      | ColumnAdd   | market      | 0     | Int         |        |             |
      | ColumnAdd   | count       | 1     | Int         |        |             |
      | ColumnAdd   | notionalSum | 2     | Long        |        |             |
      | DataReset   |             |       |             |        |             |
      | RowAdd      |             | 0     |             | 10     | 60          |
      | RowAdd      |             | 1     |             | 30     | 60          |
      | RowAdd      |             | 2     |             | 40     | 60          |
    And commit
    When rows "1,2" removed from table "source"
    Then operator "groupBy1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | market | notionalSum |
      | RowUpdate |       |             | 0     | 10     | 20          |
      | RowRemove |       |             | 1     |        |             |
    And commit

  Scenario: Can group by multiple columns
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | groupBy2         | day         |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | day | notionalSum |
      | SchemaReset |             |       |             |        |     |             |
      | ColumnAdd   | market      | 0     | Int         |        |     |             |
      | ColumnAdd   | day         | 1     | Int         |        |     |             |
      | ColumnAdd   | count       | 2     | Int         |        |     |             |
      | ColumnAdd   | notionalSum | 3     | Long        |        |     |             |
      | DataReset   |             |       |             |        |     |             |
      | RowAdd      |             | 0     |             | 10     | 1   | 20          |
      | RowAdd      |             | 1     |             | 10     | 2   | 40          |
      | RowAdd      |             | 2     |             | 30     | 3   | 60          |
      | RowAdd      |             | 3     |             | 40     | 3   | 60          |
    And commit

  Scenario: New rows added to source reflected in group multiple columns table
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | groupBy2         | day         |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | market | day | notionalSum |
      | SchemaReset |             |       |             |        |     |             |
      | ColumnAdd   | market      | 0     | Int         |        |     |             |
      | ColumnAdd   | day         | 1     | Int         |        |     |             |
      | ColumnAdd   | count       | 2     | Int         |        |     |             |
      | ColumnAdd   | notionalSum | 3     | Long        |        |     |             |
      | DataReset   |             |       |             |        |     |             |
      | RowAdd      |             | 0     |             | 10     | 1   | 20          |
      | RowAdd      |             | 1     |             | 10     | 2   | 40          |
      | RowAdd      |             | 2     |             | 30     | 3   | 60          |
      | RowAdd      |             | 3     |             | 40     | 3   | 60          |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 4      | 10         | 1       | 60            |
      | 5      | 30         | 1       | 40            |
      | 6      | 30         | 1       | 60            |
      | 7      | 40         | 3       | 60            |
    Then operator "groupBy1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | day | market | notionalSum |
      | RowUpdate |       |             | 0     | 1   | 10     | 80          |
      | RowAdd    |       |             | 4     | 1   | 30     | 100         |
      | RowUpdate |       |             | 3     | 3   | 40     | 120         |
    And commit

  Scenario: Can reset data
    When operator type "groupBy" named "groupBy1" created
      | field            | value       |
      | groupBy1         | market      |
      | summaryName1     | notionalSum |
      | summaryFunction1 | sum         |
      | summaryArgument1 | notional    |
    And operator "source" output "out" plugged into "groupBy1" input "in"
    And commit
    And reset data on operator "groupBy1"
    And commit
    Then operator "groupBy1" output "out" is
      | ~Action     | ~Name       | ~TEId | ~ColumnType | count | market | notionalSum |
      | SchemaReset |             |       |             |       |        |             |
      | ColumnAdd   | market      | 0     | Int         |       |        |             |
      | ColumnAdd   | count       | 1     | Int         |       |        |             |
      | ColumnAdd   | notionalSum | 2     | Long        |       |        |             |
      | DataReset   |             |       |             |       |        |             |
      | RowAdd      |             | 0     |             | 2     | 10     | 60          |
      | RowAdd      |             | 1     |             | 1     | 30     | 60          |
      | RowAdd      |             | 2     |             | 1     | 40     | 60          |
    And commit