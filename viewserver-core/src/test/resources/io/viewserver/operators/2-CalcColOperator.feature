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
	And commit
	Then schema for "calc1" is
	  | ~Action   | ~Name       | ~ColumnType |
	  | ColumnAdd | id          | Int         |
	  | ColumnAdd | market1     | Int         |
	  | ColumnAdd | market2     | Int         |
	  | ColumnAdd | product     | Int         |
	  | ColumnAdd | description | String      |
	  | ColumnAdd | calcCol1    | Int         |
	Then data for "calc1" is
	  | ~Action | calcCol1 | description | id | market1 | market2 | product |
	  | RowAdd  | 12       | desc1       | 0  | 2       | 20      | 10      |
	  | RowAdd  | 24       | desc2       | 1  | 4       | 40      | 20      |
	  | RowAdd  | 36       | desc3       | 2  | 6       | 60      | 30      |

  Scenario: Can augment a string value
	When operator type "calc" named "calc1" created
	  | field           | value                |
	  | calcColumnName1 | calcCol1             |
	  | calcExpression1 | description + "_ABC" |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action | calcCol1  | description | id | market1 | market2 | product |
	  | RowAdd  | desc1_ABC | desc1       | 0  | 2       | 20      | 10      |
	  | RowAdd  | desc2_ABC | desc2       | 1  | 4       | 40      | 20      |
	  | RowAdd  | desc3_ABC | desc3       | 2  | 6       | 60      | 30      |


  Scenario: Can filter a calculated column
	When operator type "calc" named "calc1" created
	  | field           | value             |
	  | calcColumnName1 | calcCol1          |
	  | calcExpression1 | market1 + product |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action | calcCol1 | id | market1 | market2 | product |
	  | RowAdd  | 12       | 0  | 2       | 20      | 10      |
	  | RowAdd  | 24       | 1  | 4       | 40      | 20      |
	  | RowAdd  | 36       | 2  | 6       | 60      | 30      |
	When operator type "filter" named "filter1" created
	  | field            | value          |
	  | filterMode       | Filter         |
	  | filterExpression | calcCol1 == 24 |
	And operator "calc1" output "out" plugged into "filter1" input "in"
	And commit
	Then data for "filter1" is
	  | ~Action | calcCol1 | id | market1 | market2 | product |
	  | RowAdd  | 24       | 1  | 4       | 40      | 20      |


  Scenario: Can calculate multiple columns
	When operator type "calc" named "calc1" created
	  | field           | value                         |
	  | calcColumnName1 | calcCol1                      |
	  | calcExpression1 | market1 + product             |
	  | calcColumnName2 | calcCol2                      |
	  | calcExpression2 | market1 * (market2 + product) |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action | calcCol1 | calcCol2 | id | market1 | market2 | product |
	  | RowAdd  | 12       | 60       | 0  | 2       | 20      | 10      |
	  | RowAdd  | 24       | 240      | 1  | 4       | 40      | 20      |
	  | RowAdd  | 36       | 540      | 2  | 6       | 60      | 30      |

  Scenario: Can calculate based on calculated column
	When operator type "calc" named "calc1" created
	  | field           | value              |
	  | calcColumnName1 | calcCol1           |
	  | calcExpression1 | market1 + product  |
	  | calcColumnName2 | calcCol2           |
	  | calcExpression2 | market1 * calcCol1 |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action     | calcCol1 | calcCol2 | id | market1 | market2 | product |
	  | RowAdd      | 12       | 24       | 0  | 2       | 20      | 10      |
	  | RowAdd      | 24       | 96       | 1  | 4       | 40      | 20      |
	  | RowAdd      | 36       | 216      | 2  | 6       | 60      | 30      |


  Scenario: Newly added rows are inserted and calculated
	When operator type "calc" named "calc1" created
	  | field           | value              |
	  | calcColumnName1 | calcCol1           |
	  | calcExpression1 | market1 + product  |
	  | calcColumnName2 | calcCol2           |
	  | calcExpression2 | market1 * calcCol1 |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action   | calcCol1 | calcCol2 | id | market1 | market2 | product |
	  | RowAdd    | 12       | 24       | 0  | 2       | 20      | 10      |
	  | RowAdd    | 24       | 96       | 1  | 4       | 40      | 20      |
	  | RowAdd    | 36       | 216      | 2  | 6       | 60      | 30      |
	And commit
	When table "source" updated to
	  | id~Int | market1~Int | market2~Int | product~Int |
	  | 3      | 2           | 20          | 10          |
	  | 4      | 4           | 40          | 20          |
	  | 5      | 6           | 60          | 30          |
	And commit
	Then data for "calc1" is
	  | ~Action | calcCol1 | calcCol2 | id | market1 | market2 | product |
	  | RowAdd  | 12       | 24       | 3  | 2       | 20      | 10      |
	  | RowAdd  | 24       | 96       | 4  | 4       | 40      | 20      |
	  | RowAdd  | 36       | 216      | 5  | 6       | 60      | 30      |


  Scenario: Calculated columns invalid when a base column is removed
	When operator type "calc" named "calc1" created
	  | field           | value              |
	  | calcColumnName1 | calcCol1           |
	  | calcExpression1 | market1 + product  |
	  | calcColumnName2 | calcCol2           |
	  | calcExpression2 | market1 * calcCol1 |
	And operator "source" output "out" plugged into "calc1" input "in"
	And commit
	Then data for "calc1" is
	  | ~Action   | calcCol1 | calcCol2 | id | market1 | market2 | product |
	  | RowAdd    | 12       | 24       | 0  | 2       | 20      | 10      |
	  | RowAdd    | 24       | 96       | 1  | 4       | 40      | 20      |
	  | RowAdd    | 36       | 216      | 2  | 6       | 60      | 30      |
	And commit
	When columns "market1" removed from table "source"
	And commit
	Then schema for "calc1" is
	  | ~Action      | ~Name    | ~ColumnType |
	  | ColumnRemove | market1  | Int         |
	  | ColumnRemove | calcCol1 | Int         |
	  | ColumnRemove | calcCol2 | Long        |

