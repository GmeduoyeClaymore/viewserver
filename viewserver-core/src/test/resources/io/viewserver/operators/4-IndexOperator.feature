Feature: Index operator fixture

  Background:
	Given id field is "market"
    Given table named "source" with data
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 1       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 3       | 60            |
      | 3      | 40         | 3       | 60            |


  Scenario: Can index a single column with multiple values
	Given output is "market~10,40"
	When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
    And operator "source" output "out" plugged into "index1" input "in"
    And commit
    Then schema for "index1" is
      | ~Action     | ~Name    |  ~ColumnType |
      | ColumnAdd   | id       |  Int         |
      | ColumnAdd   | market   |  Int         |
      | ColumnAdd   | day      |  Int         |
      | ColumnAdd   | notional |  Long        |
    Then data for "index1" is
      | ~Action     | market | notional | day |
      | RowAdd      | 10     | 20       | 1   |
      | RowAdd      | 10     | 40       | 2   |
      | RowAdd      | 40     | 60       | 3   |

  Scenario: Can update index to change output
	Given output is "market~10,30"
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
    And operator "source" output "out" plugged into "index1" input "in"
	And commit
	Then schema for "index1" is
	  | ~Action     | ~Name    | ~ColumnType |
	  | ColumnAdd   | id       | Int         |
	  | ColumnAdd   | market   | Int         |
	  | ColumnAdd   | day      | Int         |
	  | ColumnAdd   | notional | Long        |
	Then data for "index1" is
      | ~Action     | day | market | notional |
      | RowAdd      | 1   | 10     | 20       |
      | RowAdd      | 2   | 10     | 40       |
      | RowAdd      | 3   | 30     | 60       |

  Scenario: Can index a multiple columns with multiple values
	Given output is "market~10,40#day~2"
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
	And commit
    Then data for "index1" is
      | ~Action     | market | notional | day |
      | RowAdd      | 10     | 40       | 2   |

  Scenario: Updated rows reflected in indexed table output
	Given output is "market~10,40#day~2"
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
	And commit
    Then data for "index1" is
      | ~Action     | market | notional | day |
      | RowAdd      | 10     | 40       | 2   |
    And commit
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 0      | 10         | 2       | 20            |
      | 1      | 10         | 2       | 40            |
      | 2      | 30         | 2       | 60            |
      | 3      | 40         | 2       | 60            |
	And commit
    Then data for "index1" is
      | ~Action   | day | market | notional |
      | RowAdd    | 2   | 10     | 20       |
      | RowUpdate | 2   | 10     | 40       |
      | RowAdd    | 2   | 40     | 60       |

  Scenario: New rows added to source reflected in indexed table output
	Given output is "market~10,40#day~2"
    When operator type "index" named "index1" created
      | field  | value  |
      | index1 | market |
      | index2 | day    |
    And operator "source" output "out" plugged into "index1" input "in"
	And commit
    Then data for "index1" is
      | ~Action     | market | notional | day |
      | RowAdd      | 10     | 40       | 2   |
    When table "source" updated to
      | id~Int | market~Int | day~Int | notional~Long |
      | 4      | 10         | 1       | 20            |
      | 5      | 10         | 2       | 40            |
      | 6      | 30         | 3       | 60            |
      | 7      | 40         | 2       | 60            |
	And commit
	Then data for "index1" is
	  | ~Action | day | market | notional |
      | RowAdd  | 2   | 10     | 40       |
      | RowAdd  | 2   | 40     | 60       |
    And commit


