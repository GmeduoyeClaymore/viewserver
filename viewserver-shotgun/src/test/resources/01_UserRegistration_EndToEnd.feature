Feature: User registration content type feature

  Background:
    Given an in-process viewserver with 2 slave nodes
    And a data source defined by "distributed_aggregations_datasource.json"
    And a report defined by "distributed_aggregations_report.json"
    And a client connected to "inproc://master"
    Given controller "driverController "action  named "registerDriver" with data "driverRegistrion.json"


  Scenario: Can see driver in drivers report
      Given report parameters
        | Name     | Type   | Value |
        | measures | String | sum   |
      Given dimension filters
        | Name             | Type   | Value |
        | dimension_userId | String | sum   |

      When I subscribe to report "distributed_aggregations_report"
      Then the following data is received
        | customer | value_sum |
        | One      | 3000      |

    When operator type "sort" named "sort1" created
      | field           | value                  |
      | sortDescriptors | marketRank~market~desc |
      | start           | 0                      |
      | end             | 100                    |
    And operator "source" output "out" plugged into "sort1" input "in"
    Then operator "sort1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
      | SchemaReset |             |             |       |     |    |        |            |          |
      | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
      | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
      | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
      | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
      | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
      | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
      | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
      | DataReset   |             |             |       |     |    |        |            |          |
      | RowAdd      |             |             | 0     | 1   | 0  | 100    | 0          | 20       |
      | RowAdd      |             |             | 1     | 2   | 1  | 100    | 1          | 40       |
      | RowAdd      |             |             | 2     | 3   | 2  | 30     | 5          | 60       |
      | RowAdd      |             |             | 3     | 3   | 3  | 40     | 2          | 60       |
      | RowAdd      |             |             | 4     | 5   | 4  | 40     | 3          | 60       |
      | RowAdd      |             |             | 5     | 2   | 5  | 40     | 4          | 60       |
    And commit

  Scenario: Can sort single number column ascending
    When operator type "sort" named "sort1" created
      | field           | value                 |
      | sortDescriptors | marketRank~market~asc |
      | start           | 0                     |
      | end             | 100                   |
    And operator "source" output "out" plugged into "sort1" input "in"
    Then operator "sort1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
      | SchemaReset |             |             |       |     |    |        |            |          |
      | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
      | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
      | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
      | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
      | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
      | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
      | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
      | DataReset   |             |             |       |     |    |        |            |          |
      | RowAdd      |             |             | 0     | 1   | 0  | 100    | 4          | 20       |
      | RowAdd      |             |             | 1     | 2   | 1  | 100    | 5          | 40       |
      | RowAdd      |             |             | 2     | 3   | 2  | 30     | 0          | 60       |
      | RowAdd      |             |             | 3     | 3   | 3  | 40     | 1          | 60       |
      | RowAdd      |             |             | 4     | 5   | 4  | 40     | 2          | 60       |
      | RowAdd      |             |             | 5     | 2   | 5  | 40     | 3          | 60       |
    And commit

  Scenario: Rows have correct rank based on sort after updating rows on descending table
    When operator type "sort" named "sort1" created
      | field           | value                  |
      | sortDescriptors | marketRank~market~desc |
      | start           | 0                      |
      | end             | 100                    |
    And operator "source" output "out" plugged into "sort1" input "in"
    Then operator "sort1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
      | SchemaReset |             |             |       |     |    |        |            |          |
      | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
      | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
      | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
      | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
      | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
      | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
      | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
      | DataReset   |             |             |       |     |    |        |            |          |
      | RowAdd      |             |             | 0     | 1   | 0  | 100    | 0          | 20       |
      | RowAdd      |             |             | 1     | 2   | 1  | 100    | 1          | 40       |
      | RowAdd      |             |             | 2     | 3   | 2  | 30     | 5          | 60       |
      | RowAdd      |             |             | 3     | 3   | 3  | 40     | 2          | 60       |
      | RowAdd      |             |             | 4     | 5   | 4  | 40     | 3          | 60       |
      | RowAdd      |             |             | 5     | 2   | 5  | 40     | 4          | 60       |
    And commit
    When table "source" updated to
     | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
     | 3      | 999         | 1       | 20            | a                  | ccc         |
   Then operator "sort1" output "out" is
    | ~Action     | ~TEId | day | id | market | marketRank | notional |
    | RowUpdate      |1      | 1   | 0  | 100    | 1          | 20       |
    | RowUpdate      |2      | 2   | 1  | 100    | 2          | 40       |
    | RowUpdate      |3      | 1   | 3  | 999    | 0          | 20       |

   And commit

     Scenario: Rows have correct rank based on sort after updating rows on ascending table
       When operator type "sort" named "sort1" created
         | field           | value                  |
         | sortDescriptors | marketRank~market~asc |
         | start           | 0                      |
         | end             | 100                    |
       And operator "source" output "out" plugged into "sort1" input "in"
       Then operator "sort1" output "out" is
         | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
         | SchemaReset |             |             |       |     |    |        |            |          |
         | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
         | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
         | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
         | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
         | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
         | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
         | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
         | DataReset   |             |             |       |     |    |        |            |          |
         | RowAdd      |             |             | 0     | 1   | 0  | 100    | 4          | 20       |
         | RowAdd      |             |             | 1     | 2   | 1  | 100    | 5          | 40       |
         | RowAdd      |             |             | 2     | 3   | 2  | 30     | 0          | 60       |
         | RowAdd      |             |             | 3     | 3   | 3  | 40     | 1          | 60       |
         | RowAdd      |             |             | 4     | 5   | 4  | 40     | 2          | 60       |
         | RowAdd      |             |             | 5     | 2   | 5  | 40     | 3          | 60       |
       And commit
       When table "source" updated to
        | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
        | 0      | 999         | 1       | 20            | a                  | ccc         |
      Then operator "sort1" output "out" is
       | ~Action     | ~TEId | day | id | market | marketRank | notional |
       | RowUpdate      |1      | 2   | 1  | 100    | 4          | 40       |
       | RowUpdate      |5      | 1   | 0  | 999    | 5          | 20       |
      And commit

   Scenario: Rows have correct rank based on sort after adding rows
       When operator type "sort" named "sort1" created
         | field           | value                  |
         | sortDescriptors | marketRank~market~desc |
         | start           | 0                      |
         | end             | 100                    |
       And operator "source" output "out" plugged into "sort1" input "in"
       Then operator "sort1" output "out" is
         | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
         | SchemaReset |             |             |       |     |    |        |            |          |
         | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
         | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
         | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
         | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
         | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
         | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
         | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
         | DataReset   |             |             |       |     |    |        |            |          |
         | RowAdd      |             |             | 0     | 1   | 0  | 100    | 0          | 20       |
         | RowAdd      |             |             | 1     | 2   | 1  | 100    | 1          | 40       |
         | RowAdd      |             |             | 2     | 3   | 2  | 30     | 5          | 60       |
         | RowAdd      |             |             | 3     | 3   | 3  | 40     | 2          | 60       |
         | RowAdd      |             |             | 4     | 5   | 4  | 40     | 3          | 60       |
         | RowAdd      |             |             | 5     | 2   | 5  | 40     | 4          | 60       |
       And commit
       When table "source" updated to
         | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
         | 6      | 10         | 1       | 20            | a                  | ccc         |
       Then operator "sort1" output "out" is
         | ~Action   | ~Name | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
         | RowAdd    |       |             | 6     | 1   | 6  | 10     | 6          | 20       |
       And commit

  Scenario: Rows have correct rank based on sort after rows are removed
    When operator type "sort" named "sort1" created
      | field           | value                  |
      | sortDescriptors | marketRank~market~desc |
      | start           | 0                      |
      | end             | 100                    |
    And operator "source" output "out" plugged into "sort1" input "in"
    Then operator "sort1" output "out" is
      | ~Action     | ~Name       | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
      | SchemaReset |             |             |       |     |    |        |            |          |
      | ColumnAdd   | id          | Int         | 0     |     |    |        |            |          |
      | ColumnAdd   | market      | Int         | 1     |     |    |        |            |          |
      | ColumnAdd   | day         | Int         | 2     |     |    |        |            |          |
      | ColumnAdd   | notional    | Long        | 3     |     |    |        |            |          |
      | ColumnAdd   | description | String      | 4     |     |    |        |            |          |
      | ColumnAdd   | code        | String      | 5     |     |    |        |            |          |
      | ColumnAdd   | marketRank  | Int         | 6     |     |    |        |            |          |
      | DataReset   |             |             |       |     |    |        |            |          |
      | RowAdd      |             |             | 0     | 1   | 0  | 100    | 0          | 20       |
      | RowAdd      |             |             | 1     | 2   | 1  | 100    | 1          | 40       |
      | RowAdd      |             |             | 2     | 3   | 2  | 30     | 5          | 60       |
      | RowAdd      |             |             | 3     | 3   | 3  | 40     | 2          | 60       |
      | RowAdd      |             |             | 4     | 5   | 4  | 40     | 3          | 60       |
      | RowAdd      |             |             | 5     | 2   | 5  | 40     | 4          | 60       |
    And commit
    When rows "1,4" removed from table "source"
    Then operator "sort1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | day | id | market | marketRank | notional |
      | RowRemove |       |             | 1     |     |    |        |            |          |
      | RowRemove |       |             | 4     |     |    |        |            |          |
      | RowUpdate |       |             | 2     | 3   | 2  | 30     | 3          | 60       |
      | RowUpdate |       |             | 3     | 3   | 3  | 40     | 1          | 60       |
      | RowUpdate |       |             | 5     | 2   | 5  | 40     | 2          | 60       |
    And commit



  Scenario: Can sort single string column
    When operator type "sort" named "sort1" created
      | field           | value                            |
      | sortDescriptors | descriptionRank~description~desc |
      | start           | 0                                |
      | end             | 100                              |
    And operator "source" output "out" plugged into "sort1" input "in"
    Then operator "sort1" output "out" is
      | ~Action     | ~Name           | ~ColumnType | ~TEId | day | description | descriptionRank | id | market | notional |
      | SchemaReset |                 |             |       |     |             |                 |    |        |          |
      | ColumnAdd   | id              | Int         | 0     |     |             |                 |    |        |          |
      | ColumnAdd   | market          | Int         | 1     |     |             |                 |    |        |          |
      | ColumnAdd   | day             | Int         | 2     |     |             |                 |    |        |          |
      | ColumnAdd   | notional        | Long        | 3     |     |             |                 |    |        |          |
      | ColumnAdd   | description     | String      | 4     |     |             |                 |    |        |          |
      | ColumnAdd   | code            | String      | 5     |     |             |                 |    |        |          |
      | ColumnAdd   | descriptionRank | Int         | 6     |     |             |                 |    |        |          |
      | DataReset   |                 |             |       |     |             |                 |    |        |          |
      | RowAdd      |                 |             | 0     | 1   | a           | 5               | 0  | 100    | 20       |
      | RowAdd      |                 |             | 1     | 2   | f           | 0               | 1  | 100    | 40       |
      | RowAdd      |                 |             | 2     | 3   | c           | 3               | 2  | 30     | 60       |
      | RowAdd      |                 |             | 3     | 3   | e           | 1               | 3  | 40     | 60       |
      | RowAdd      |                 |             | 4     | 5   | d           | 2               | 4  | 40     | 60       |
      | RowAdd      |                 |             | 5     | 2   | b           | 4               | 5  | 40     | 60       |
    And commit
