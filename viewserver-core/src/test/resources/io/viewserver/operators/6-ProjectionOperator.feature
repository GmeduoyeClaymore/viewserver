Feature: Projection operator fixture

  Background:
	Given table named "source" with data
	  | id~Int | market~Int | day~Int | notional~Long |
	  | 0      | 10         | 1       | 20            |
	  | 1      | 10         | 2       | 40            |
	  | 2      | 30         | 3       | 60            |
	  | 3      | 40         | 3       | 60            |


  Scenario: Can rename columns
	When operator type "projection" named "projection1" created
	  | field       | value                                           |
	  | projections | market=market_renamed,notional=notional_renamed |
	And operator "source" output "out" plugged into "projection1" input "in"
	And commit
	Then schema for "projection1" is
	  | ~Action     | ~Name            | ~ColumnType |
	  | ColumnAdd   | id               | Int         |
	  | ColumnAdd   | market_renamed   | Int         |
	  | ColumnAdd   | day              | Int         |
	  | ColumnAdd   | notional_renamed | Long        |
	And  data for "projection1" is
	  | ~Action | market_renamed | notional_renamed | day | id |
	  | RowAdd  | 10             | 20               | 1   | 0  |
	  | RowAdd  | 10             | 40               | 2   | 1  |
	  | RowAdd  | 30             | 60               | 3   | 2  |
	  | RowAdd  | 40             | 60               | 3   | 3  |
	And commit


  Scenario: Can explicitly include columns
	Given id field is "market"
	When operator type "projection" named "projection1" created
	  | field    | value  |
	  | included | market |
	And operator "source" output "out" plugged into "projection1" input "in"
	And commit
	Then schema for "projection1" is
	  | ~Action     | ~Name  | ~ColumnType |
	  | ColumnAdd   | market | Int         |
	Then data for "projection1" is
	  | ~Action     | market |
	  | RowAdd      | 10     |
	  | RowAdd      | 10     |
	  | RowAdd      | 30     |
	  | RowAdd      | 40     |
	And commit


  Scenario: Can explicitly exclude columns
	When operator type "projection" named "projection1" created
	  | field    | value  |
	  | excluded | market |
	And operator "source" output "out" plugged into "projection1" input "in"
	And commit
	Then schema for "projection1" is
	  | ~Action     | ~Name    | ~ColumnType |
	  | ColumnAdd   | id       | Int         |
	  | ColumnAdd   | day      | Int         |
	  | ColumnAdd   | notional | Long        |
	Then data for "projection1" is
	  | ~Action     | notional | day | id |
	  | RowAdd      | 20       | 1   | 0  |
	  | RowAdd      | 40       | 2   | 1  |
	  | RowAdd      | 60       | 3   | 2  |
	  | RowAdd      | 60       | 3   | 3  |
	And commit

