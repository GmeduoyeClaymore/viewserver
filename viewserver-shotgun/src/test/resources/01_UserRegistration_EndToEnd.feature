Feature: User registration content type feature

  Background:
    Given a running shotgun viewserver
    Given a client connected to "inproc://master"
    Given controller "driverController" action "registerDriver" invoked with data file "driverRegistration.json"


  Scenario: Can see driver for product in userProduct report
      Given report parameters
        | Name           | Type    | Value |
        | showOutOfRange | Boolean | false |
        | showUnrelated  | Boolean | true  |
        | latitude       | Integer | 0     |
        | longitude      | Integer | 0     |
        | userId         | Integer | 0     |
      Given dimension filters
        | Name             | Type   | Value      |
        | dimension_productId | String | BrickLayer |
      And paging from 0 to 100 by "userId" descending
      When I subscribe to report "usersForProductAll"
      Then the following data is received
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
