Feature: GroupBy operator fixture

  Background:
	Given id field is "market"
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
	And commit
	Then schema for "groupBy1" is
	  | ~Action   | ~Name       | ~ColumnType |
	  | ColumnAdd | market      | Int         |
	  | ColumnAdd | count       | Int         |
	  | ColumnAdd | notionalSum | Long        |
	And data for "groupBy1" is
	  | ~Action | market | notionalSum |
	  | RowAdd  | 10     | 60          |
	  | RowAdd  | 30     | 60          |
	  | RowAdd  | 40     | 60          |

  Scenario: Edited source is reflected in group by table
	When operator type "groupBy" named "groupBy1" created
	  | field            | value       |
	  | groupBy1         | market      |
	  | summaryName1     | notionalSum |
	  | summaryFunction1 | sum         |
	  | summaryArgument1 | notional    |
	And operator "source" output "out" plugged into "groupBy1" input "in"
	And commit
	Then data for "groupBy1" is
	  | ~Action | market | notionalSum |
	  | RowAdd  | 10     | 60          |
	  | RowAdd  | 30     | 60          |
	  | RowAdd  | 40     | 60          |
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 0      | 10         | 1       | 60            |
	  | 1      | 30         | 2       | 40            |
	  | 2      | 30         | 3       | 60            |
	  | 3      | 40         | 3       | 60            |
	And commit
	Then data for "groupBy1" is
	  | ~Action   | market | notionalSum |
	  | RowUpdate | 10     | 60          |
	  | RowUpdate | 30     | 100         |

  Scenario: New rows added to source reflected in group by table
	When operator type "groupBy" named "groupBy1" created
	  | field            | value       |
	  | groupBy1         | market      |
	  | summaryName1     | notionalSum |
	  | summaryFunction1 | sum         |
	  | summaryArgument1 | notional    |
	And operator "source" output "out" plugged into "groupBy1" input "in"
	And commit
	Then data for "groupBy1" is
	  | ~Action | market | notionalSum |
	  | RowAdd  | 10     | 60          |
	  | RowAdd  | 30     | 60          |
	  | RowAdd  | 40     | 60          |
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 4      | 10         | 1       | 60            |
	  | 5      | 30         | 2       | 40            |
	  | 6      | 30         | 3       | 60            |
	  | 7      | 40         | 3       | 60            |
	And commit
	Then data for "groupBy1" is
	  | ~Action   | market | notionalSum |
	  | RowUpdate | 10     | 120         |
	  | RowUpdate | 30     | 160         |
	  | RowUpdate | 40     | 120         |

  Scenario: Rows deleted from source reflected in group by table
	When operator type "groupBy" named "groupBy1" created
	  | field            | value       |
	  | groupBy1         | market      |
	  | summaryName1     | notionalSum |
	  | summaryFunction1 | sum         |
	  | summaryArgument1 | notional    |
	And operator "source" output "out" plugged into "groupBy1" input "in"
	And commit
	Then data for "groupBy1" is
	  | ~Action | market | notionalSum |
	  | RowAdd  | 10     | 60          |
	  | RowAdd  | 30     | 60          |
	  | RowAdd  | 40     | 60          |
	When rows "1,2" removed from table "source"
	And commit
	Then data for "groupBy1" is
	  | ~Action   | market | notionalSum |
	  | RowUpdate | 10     | 20          |
	  | RowRemove | 30       |             |

  Scenario: Can group by multiple columns
	When operator type "groupBy" named "groupBy1" created
	  | field            | value       |
	  | groupBy1         | market      |
	  | groupBy2         | day         |
	  | summaryName1     | notionalSum |
	  | summaryFunction1 | sum         |
	  | summaryArgument1 | notional    |
	And operator "source" output "out" plugged into "groupBy1" input "in"
	And commit
	Then schema for "groupBy1" is
	  | ~Action     | ~Name       | ~ColumnType |
	  | ColumnAdd   | market      | Int         |
	  | ColumnAdd   | day         | Int         |
	  | ColumnAdd   | count       | Int         |
	  | ColumnAdd   | notionalSum | Long        |
	Then data for "groupBy1" is
	  | ~Action     | market | day | notionalSum |
	  | RowAdd      | 10     | 1   | 20          |
	  | RowAdd      | 10     | 2   | 40          |
	  | RowAdd      | 30     | 3   | 60          |
	  | RowAdd      | 40     | 3   | 60          |
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
	And commit
	Then data for "groupBy1" is
	  | ~Action     | market | day | notionalSum |
	  | RowAdd      | 10     | 1   | 20          |
	  | RowAdd      | 10     | 2   | 40          |
	  | RowAdd      | 30     | 3   | 60          |
	  | RowAdd      | 40     | 3   | 60          |
	When table "source" updated to
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 4      | 10         | 1       | 60            |
	  | 5      | 30         | 1       | 40            |
	  | 6      | 30         | 1       | 60            |
	  | 7      | 40         | 3       | 60            |
	And commit
	Then data for "groupBy1" is
	  | ~Action   | day | market | notionalSum |
	  | RowUpdate | 1   | 10     | 80          |
	  | RowAdd    | 1   | 30     | 100         |
	  | RowUpdate | 3   | 40     | 120         |

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
	Then data for "groupBy1" is
	  | ~Action     | count | market | notionalSum |
	  | RowAdd      | 2     | 10     | 60          |
	  | RowAdd      | 1     | 30     | 60          |
	  | RowAdd      | 1     | 40     | 60          |
