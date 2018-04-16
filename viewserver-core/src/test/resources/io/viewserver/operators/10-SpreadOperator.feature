Feature: Spread operator fixture

  Background:
    Given table named "source" with data
      | id~Int | market~String | product~String |
      | 0      | Market1       | Product1,Product2       |
      | 1      | Market1       | Product2       |
      | 2      | Market1       | Product3,Product3       |
      | 3      | Market1       | Product1       |

    When operator type "spread" named "spread1" created
      | field            | value         |
      | inputColumn  | product        |
      | outputColumn | spread |
      | spreadFunction | csv |
      | removeInputColumn | false |
    And operator "source" output "out" plugged into "spread1" input "in"
    And listen for changes on "spread1" output "out"
    And commit

  Scenario: Can spread CSV column
    When operator type "spread" named "spread2" created
        | field            | value         |
        | inputColumn  | product        |
        | outputColumn | spread |
        | spreadFunction | csv |
        | removeInputColumn | false |
    And operator "source" output "out" plugged into "spread2" input "in"
    And listen for changes on "spread2" output "out"
    And commit
    Then operator "spread2" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread|
      | SchemaReset |           |             |       |    |         |        |
      | ColumnAdd   | id        | Int         | 0     |    |         |        |
      | ColumnAdd   | market    | String      | 1     |    |         |        |
      | ColumnAdd   | spread   | String       | 2     |    |         |        |
      | ColumnAdd   | product   | String      | 3     |    |         |        |
      | DataReset   |           |             |       |    |         |        |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product1 |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product2 |
      | RowAdd      |           |             | 1     | 1  | Market1 | Product2 |
      | RowAdd      |           |             | 2     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 3     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 4     | 3  | Market1 | Product1 |
    And commit



  Scenario: Updated row is correctly reflected in spread operator
    When operator type "spread" named "spread2" created
        | field            | value         |
        | inputColumn  | product        |
        | outputColumn | spread |
        | spreadFunction | csv |
        | removeInputColumn | false |
    And operator "source" output "out" plugged into "spread2" input "in"
    And listen for changes on "spread2" output "out"
    And commit
    Then operator "spread2" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread|
      | SchemaReset |           |             |       |    |         |        |
      | ColumnAdd   | id        | Int         | 0     |    |         |        |
      | ColumnAdd   | market    | String      | 1     |    |         |        |
      | ColumnAdd   | spread   | String       | 2     |    |         |        |
      | ColumnAdd   | product   | String      | 3     |    |         |        |
      | DataReset   |           |             |       |    |         |        |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product1 |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product2 |
      | RowAdd      |           |             | 1     | 1  | Market1 | Product2 |
      | RowAdd      |           |             | 2     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 3     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 4     | 3  | Market1 | Product1 |
    And commit
    When table "source" updated to
     | id~Int | product~String     |
     | 2      |             |
     Then operator "spread2" output "out" is
       | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread   |
       | RowRemove   |           |             | 0     | 2  | Market1 | Product3 |
       | RowRemove  |           |             | 1     | 2  | Market1 | Product3 |
     And commit


     Scenario: Removed row is correctly reflected in spread operator
       When operator type "spread" named "spread2" created
           | field            | value         |
           | inputColumn  | product        |
           | outputColumn | spread |
           | spreadFunction | csv |
           | removeInputColumn | false |
       And operator "source" output "out" plugged into "spread2" input "in"
       And listen for changes on "spread2" output "out"
       And commit
       Then operator "spread2" output "out" is
         | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread|
         | SchemaReset |           |             |       |    |         |        |
         | ColumnAdd   | id        | Int         | 0     |    |         |        |
         | ColumnAdd   | market    | String      | 1     |    |         |        |
         | ColumnAdd   | spread   | String       | 2     |    |         |        |
         | ColumnAdd   | product   | String      | 3     |    |         |        |
         | DataReset   |           |             |       |    |         |        |
         | RowAdd      |           |             | 0     | 0  | Market1 | Product1 |
         | RowAdd      |           |             | 0     | 0  | Market1 | Product2 |
         | RowAdd      |           |             | 1     | 1  | Market1 | Product2 |
         | RowAdd      |           |             | 2     | 2  | Market1 | Product3 |
         | RowAdd      |           |             | 3     | 2  | Market1 | Product3 |
         | RowAdd      |           |             | 4     | 3  | Market1 | Product1 |
       And commit
       When rows "1,2" removed from table "source"
        Then operator "spread2" output "out" is
          | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread   |
          | RowRemove   |           |             | 0     | 2  | Market1 | Product3 |
          | RowRemove  |           |             | 1     | 2  | Market1 | Product3 |
          | RowRemove  |           |             | 2     | 1  | Market1 | Product2 |
        And commit


  Scenario: Added row is correctly reflected in spread operator
    When operator type "spread" named "spread2" created
        | field            | value         |
        | inputColumn  | product        |
        | outputColumn | spread |
        | spreadFunction | csv |
        | removeInputColumn | false |
    And operator "source" output "out" plugged into "spread2" input "in"
    And listen for changes on "spread2" output "out"
    And commit
    Then operator "spread2" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | id | market  | spread|
      | SchemaReset |           |             |       |    |         |        |
      | ColumnAdd   | id        | Int         | 0     |    |         |        |
      | ColumnAdd   | market    | String      | 1     |    |         |        |
      | ColumnAdd   | spread   | String       | 2     |    |         |        |
      | ColumnAdd   | product   | String      | 3     |    |         |        |
      | DataReset   |           |             |       |    |         |        |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product1 |
      | RowAdd      |           |             | 0     | 0  | Market1 | Product2 |
      | RowAdd      |           |             | 1     | 1  | Market1 | Product2 |
      | RowAdd      |           |             | 2     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 3     | 2  | Market1 | Product3 |
      | RowAdd      |           |             | 4     | 3  | Market1 | Product1 |
    And commit
    When table "source" updated to
     | id~Int | product~String     |
     | 4      |   foo1,foo2          |
     Then operator "spread2" output "out" is
       | ~Action     | ~Name     | ~ColumnType | ~TEId | id | spread   |
       | RowAdd      |           |             | 0     | 4  | foo1 |
       | RowAdd      |           |             | 1     | 4  | foo2 |
     And commit

