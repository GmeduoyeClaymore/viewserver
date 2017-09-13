Feature: CalcCol operator fixture

  Background:
    Given table named "source" with data
      | id~Int | market1~Int | market2~Int | product~Int | description~String |
      | 0      | 2           | 20          | 10          | desc1              |
      | 1      | 4           | 40          | 20          | desc2              |
      | 2      | 6           | 60          | 30          | desc3              |
    #And listen for changes on "calc1" output "out"

  Scenario: Can calculate single column
    When operator type "calc" named "calc1" created
      | field           | value             |
      | calcColumnName1 | calcCol1          |
      | calcExpression1 | market1 + product |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | description | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |             |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |             |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |             |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |             |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |             |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |             |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |             |    |         |         |         |
      | DataReset   |             |             |       |          |             |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | desc1       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | desc2       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | desc3       | 2  | 6       | 60      | 30      |
    And commit

  Scenario: Can augment a string value
    When operator type "calc" named "calc1" created
      | field           | value                |
      | calcColumnName1 | calcCol1             |
      | calcExpression1 | description + "_ABC" |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1  | description | id | market1 | market2 | product |
      | SchemaReset |             |             |       |           |             |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |           |             |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |           |             |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |           |             |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |           |             |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |           |             |    |         |         |         |
      | ColumnAdd   | calcCol1    | String      | 5     |           |             |    |         |         |         |
      | DataReset   |             |             |       |           |             |    |         |         |         |
      | RowAdd      |             |             | 0     | desc1_ABC | desc1       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | desc2_ABC | desc2       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | desc3_ABC | desc3       | 2  | 6       | 60      | 30      |
    And commit

  Scenario: Can filter a calculated column
    When operator type "calc" named "calc1" created
      | field           | value             |
      | calcColumnName1 | calcCol1          |
      | calcExpression1 | market1 + product |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |    |         |         |         |
      | DataReset   |             |             |       |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | 2  | 6       | 60      | 30      |
    And commit
    When operator type "filter" named "filter1" created
      | field            | value          |
      | filterMode       | Filter         |
      | filterExpression | calcCol1 == 24 |
    And operator "calc1" output "out" plugged into "filter1" input "in"
    And listen for changes on "filter1" output "out"
    And commit
    Then operator "filter1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |    |         |         |         |
      | DataReset   |             |             |       |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 24       | 1  | 4       | 40      | 20      |
    And commit


  Scenario: Can calculate multiple columns
    When operator type "calc" named "calc1" created
      | field           | value                         |
      | calcColumnName1 | calcCol1                      |
      | calcExpression1 | market1 + product             |
      | calcColumnName2 | calcCol2                      |
      | calcExpression2 | market1 * (market2 + product) |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | calcCol2 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol2    | Long        | 6     |          |          |    |         |         |         |
      | DataReset   |             |             |       |          |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | 60       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | 240      | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | 540      | 2  | 6       | 60      | 30      |
    And commit

  Scenario: Can calculate based on calculated column
    When operator type "calc" named "calc1" created
      | field           | value              |
      | calcColumnName1 | calcCol1           |
      | calcExpression1 | market1 + product  |
      | calcColumnName2 | calcCol2           |
      | calcExpression2 | market1 * calcCol1 |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | calcCol2 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol2    | Long        | 6     |          |          |    |         |         |         |
      | DataReset   |             |             |       |          |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | 24       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | 96       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | 216      | 2  | 6       | 60      | 30      |
    And commit


  Scenario: Newly added rows are inserted and calculated
    When operator type "calc" named "calc1" created
      | field           | value              |
      | calcColumnName1 | calcCol1           |
      | calcExpression1 | market1 + product  |
      | calcColumnName2 | calcCol2           |
      | calcExpression2 | market1 * calcCol1 |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | calcCol2 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol2    | Long        | 6     |          |          |    |         |         |         |
      | DataReset   |             |             |       |          |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | 24       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | 96       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | 216      | 2  | 6       | 60      | 30      |
    And commit
    When table "source" updated to
      | id~Int | market1~Int | market2~Int | product~Int |
      | 3      | 2           | 20          | 10          |
      | 4      | 4           | 40          | 20          |
      | 5      | 6           | 60          | 30          |
    Then operator "calc1" output "out" is
      | ~Action | ~Name | ~ColumnType | ~TEId | calcCol1 | calcCol2 | id | market1 | market2 | product |
      | RowAdd  |       |             | 3     | 12       | 24       | 3  | 2       | 20      | 10      |
      | RowAdd  |       |             | 4     | 24       | 96       | 4  | 4       | 40      | 20      |
      | RowAdd  |       |             | 5     | 36       | 216      | 5  | 6       | 60      | 30      |
    And commit

  Scenario: Calculated columns invalid when a base column is removed
    When operator type "calc" named "calc1" created
      | field           | value              |
      | calcColumnName1 | calcCol1           |
      | calcExpression1 | market1 + product  |
      | calcColumnName2 | calcCol2           |
      | calcExpression2 | market1 * calcCol1 |
    And operator "source" output "out" plugged into "calc1" input "in"
    Then operator "calc1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | calcCol1 | calcCol2 | id | market1 | market2 | product |
      | SchemaReset |             |             |       |          |          |    |         |         |         |
      | ColumnAdd   | id          | Int         | 0     |          |          |    |         |         |         |
      | ColumnAdd   | market1     | Int         | 1     |          |          |    |         |         |         |
      | ColumnAdd   | market2     | Int         | 2     |          |          |    |         |         |         |
      | ColumnAdd   | product     | Int         | 3     |          |          |    |         |         |         |
      | ColumnAdd   | description | String      | 4     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol1    | Int         | 5     |          |          |    |         |         |         |
      | ColumnAdd   | calcCol2    | Long        | 6     |          |          |    |         |         |         |
      | DataReset   |             |             |       |          |          |    |         |         |         |
      | RowAdd      |             |             | 0     | 12       | 24       | 0  | 2       | 20      | 10      |
      | RowAdd      |             |             | 1     | 24       | 96       | 1  | 4       | 40      | 20      |
      | RowAdd      |             |             | 2     | 36       | 216      | 2  | 6       | 60      | 30      |
    And commit
    When columns "market1" removed from table "source"
    Then operator "calc1" output "out" is
      | ~Action      | ~Name    | ~ColumnType | ~TEId |
      | ColumnRemove | market1  | Int         | 1     |
      | ColumnRemove | calcCol1 | Int         | 5     |
      | ColumnRemove | calcCol2 | Long        | 6     |
    And commit

