Feature: Sort operator fixture

  Background:
	Given table named "source" with data
	  | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
	  | 0      | 100        | 1       | 20            | a                  | ccc         |
	  | 1      | 100        | 2       | 40            | f                  | ddd         |
	  | 2      | 30         | 3       | 60            | c                  | bbb         |
	  | 3      | 40         | 3       | 60            | e                  | eee         |
	  | 4      | 40         | 5       | 60            | d                  | fff         |
	  | 5      | 40         | 2       | 60            | b                  | aaa         |


  Scenario: Can sort single number column descending
	When operator type "sort" named "sort1" created
	  | field           | value                  |
	  | sortDescriptors | marketRank~market~desc |
	  | start           | 0                      |
	  | end             | 100                    |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then schema for "sort1" is
	  | ~Action     | ~Name       | ~ColumnType |
	  | ColumnAdd   | id          | Int         |
	  | ColumnAdd   | market      | Int         |
	  | ColumnAdd   | day         | Int         |
	  | ColumnAdd   | notional    | Long        |
	  | ColumnAdd   | description | String      |
	  | ColumnAdd   | code        | String      |
	  | ColumnAdd   | marketRank  | Int         |
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 0          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 1          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 5          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 2          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 3          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 4          | 60       |

  Scenario: Can sort single number column ascending
	When operator type "sort" named "sort1" created
	  | field           | value                 |
	  | sortDescriptors | marketRank~market~asc |
	  | start           | 0                     |
	  | end             | 100                   |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 4          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 5          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 0          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 1          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 2          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 3          | 60       |

  Scenario: Rows have correct rank based on sort after updating rows on descending table
	When operator type "sort" named "sort1" created
	  | field           | value                  |
	  | sortDescriptors | marketRank~market~desc |
	  | start           | 0                      |
	  | end             | 100                    |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 0          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 1          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 5          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 2          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 3          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 4          | 60       |
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
	  | 3      | 999        | 1       | 20            | a                  | ccc         |
	And commit
	Then data for "sort1" is
	  | ~Action   | day | id | market | marketRank | notional |
	  | RowUpdate | 1   | 0  | 100    | 1          | 20       |
	  | RowUpdate | 2   | 1  | 100    | 2          | 40       |
	  | RowUpdate | 1   | 3  | 999    | 0          | 20       |


  Scenario: Rows have correct rank based on sort after updating rows on ascending table
	When operator type "sort" named "sort1" created
	  | field           | value                 |
	  | sortDescriptors | marketRank~market~asc |
	  | start           | 0                     |
	  | end             | 100                   |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 4          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 5          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 0          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 1          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 2          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 3          | 60       |
	And commit
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
	  | 0      | 999        | 1       | 20            | a                  | ccc         |
	And commit
	Then data for "sort1" is
	  | ~Action   | day | id | market | marketRank | notional |
	  | RowUpdate | 2   | 1  | 100    | 4          | 40       |
	  | RowUpdate | 1   | 0  | 999    | 5          | 20       |

  Scenario: Rows have correct rank based on sort after adding rows
	When operator type "sort" named "sort1" created
	  | field           | value                  |
	  | sortDescriptors | marketRank~market~desc |
	  | start           | 0                      |
	  | end             | 100                    |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 0          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 1          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 5          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 2          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 3          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 4          | 60       |
	And commit
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long | description~String | code~String |
	  | 6      | 10         | 1       | 20            | a                  | ccc         |
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 6  | 10     | 6          | 20       |

  Scenario: Rows have correct rank based on sort after rows are removed
	When operator type "sort" named "sort1" created
	  | field           | value                  |
	  | sortDescriptors | marketRank~market~desc |
	  | start           | 0                      |
	  | end             | 100                    |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action | day | id | market | marketRank | notional |
	  | RowAdd  | 1   | 0  | 100    | 0          | 20       |
	  | RowAdd  | 2   | 1  | 100    | 1          | 40       |
	  | RowAdd  | 3   | 2  | 30     | 5          | 60       |
	  | RowAdd  | 3   | 3  | 40     | 2          | 60       |
	  | RowAdd  | 5   | 4  | 40     | 3          | 60       |
	  | RowAdd  | 2   | 5  | 40     | 4          | 60       |
	When rows "1,4" removed from table "source"
	And commit
	Then data for "sort1" is
	  | ~Action   | day | id | market | marketRank | notional |
	  | RowRemove |     | 1  |        |            |          |
	  | RowRemove |     | 4  |        |            |          |
	  | RowUpdate | 3   | 2  | 30     | 3          | 60       |
	  | RowUpdate | 3   | 3  | 40     | 1          | 60       |
	  | RowUpdate | 2   | 5  | 40     | 2          | 60       |


  Scenario: Can sort single string column
	When operator type "sort" named "sort1" created
	  | field           | value                            |
	  | sortDescriptors | descriptionRank~description~desc |
	  | start           | 0                                |
	  | end             | 100                              |
	And operator "source" output "out" plugged into "sort1" input "in"
	And commit
	Then data for "sort1" is
	  | ~Action     | day | description | descriptionRank | id | market | notional |
	  | RowAdd      | 1   | a           | 5               | 0  | 100    | 20       |
	  | RowAdd      | 2   | f           | 0               | 1  | 100    | 40       |
	  | RowAdd      | 3   | c           | 3               | 2  | 30     | 60       |
	  | RowAdd      | 3   | e           | 1               | 3  | 40     | 60       |
	  | RowAdd      | 5   | d           | 2               | 4  | 40     | 60       |
	  | RowAdd      | 2   | b           | 4               | 5  | 40     | 60       |
