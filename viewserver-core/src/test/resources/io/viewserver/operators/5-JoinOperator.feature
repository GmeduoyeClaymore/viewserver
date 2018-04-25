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
	And commit
	Then schema for "join1" is
	  | ~Action   | ~Name            | ~ColumnType |
	  | ColumnAdd | id               | Int         |
	  | ColumnAdd | market           | Int         |
	  | ColumnAdd | day              | Int         |
	  | ColumnAdd | notional         | Long        |
	  | ColumnAdd | source2_id       | Int         |
	  | ColumnAdd | source2_market   | Int         |
	  | ColumnAdd | source2_day      | Int         |
	  | ColumnAdd | source2_notional | Long        |
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
	  | RowAdd  | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
	  | RowAdd  | 0  | 10     | 20       | 0          | 1           | 25               | 10             |

  Scenario: Updated rows appear in joined table
	When operator type "join" named "join1" created
	  | field       | value  |
	  | leftColumn  | market |
	  | rightColumn | market |
	And operator "source1" output "out" plugged into "join1" input "left"
	And operator "source2" output "out" plugged into "join1" input "right"
	And commit
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
	  | RowAdd  | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
	  | RowAdd  | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
	When table "source1" updated to
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 0      | 10         | 1       | 20            |
	  | 1      | 10         | 2       | 40            |
	  | 2      | 10         | 3       | 60            |
	  | 3      | 10         | 3       | 60            |
	And commit
	Then data for "join1" is
	  | ~Action   | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
	  | RowUpdate | 0  | 10     | 20       | 1           | 0          | 10             | 25               |
	  | RowUpdate | 1  | 10     | 40       | 1           | 0          | 10             | 25               |
	  | RowAdd    | 2  | 10     | 60       | 1           | 0          | 10             | 25               |
	  | RowAdd    | 3  | 10     | 60       | 1           | 0          | 10             | 25               |

  Scenario: Joined table updated when row in source is deleted
	When operator type "join" named "join1" created
	  | field       | value  |
	  | leftColumn  | market |
	  | rightColumn | market |
	And operator "source1" output "out" plugged into "join1" input "left"
	And operator "source2" output "out" plugged into "join1" input "right"
	And commit
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
	  | RowAdd  | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
	  | RowAdd  | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
	And commit
	When rows "1,2" removed from table "source1"
	And commit
	Then data for "join1" is
	  | ~Action   | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
	  | RowRemove | 1  |        |          |             |            |                |                  |


  Scenario: Newly inserted rows appear in joined table
	When operator type "join" named "join1" created
	  | field       | value  |
	  | leftColumn  | market |
	  | rightColumn | market |
	And operator "source1" output "out" plugged into "join1" input "left"
	And operator "source2" output "out" plugged into "join1" input "right"
	And commit
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
	  | RowAdd  | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
	  | RowAdd  | 0  | 10     | 20       | 0          | 1           | 25               | 10             |

	When table "source1" updated to
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 4      | 10         | 1       | 20            |
	  | 5      | 10         | 2       | 40            |
	  | 6      | 10         | 3       | 60            |
	  | 7      | 10         | 3       | 60            |
	And commit
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_day | source2_id | source2_market | source2_notional |
	  | RowAdd  | 4  | 10     | 20       | 1           | 0          | 10 << 10       | 25               |
	  | RowAdd  | 5  | 10     | 40       | 1           | 0          | 10 << 10       | 25               |
	  | RowAdd  | 6  | 10     | 60       | 1           | 0          | 10 << 10       | 25               |
	  | RowAdd  | 7  | 10     | 60       | 1           | 0          | 10 << 10       | 25               |
	And commit

  #???????????????????????????????????????????????????????????
  Scenario: Joined table updated when column is removed
	When operator type "join" named "join1" created
	  | field       | value  |
	  | leftColumn  | market |
	  | rightColumn | market |
	And operator "source1" output "out" plugged into "join1" input "left"
	And operator "source2" output "out" plugged into "join1" input "right"
	And commit
	Then data for "join1" is
	  | ~Action | id | market | notional | source2_id | source2_day | source2_notional | source2_market |
	  | RowAdd  | 1  | 10     | 40       | 0          | 1           | 25               | 10             |
	  | RowAdd  | 0  | 10     | 20       | 0          | 1           | 25               | 10             |
	And commit
	When columns "day" removed from table "source1"
	When columns "day" removed from table "source2"
	And commit
	Then data for "join1" is
	  | ~Action | ~Name | ~ColumnType | ~TEId | id | market | notional | source2_day | source2_id | source2_market | source2_notional |

	And commit