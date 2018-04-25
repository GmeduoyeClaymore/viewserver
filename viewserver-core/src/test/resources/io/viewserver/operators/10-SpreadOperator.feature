Feature: Spread operator fixture

  Background:
	Given table named "source" with data
	  | id~Int | market~String | product~String    |
	  | 0      | Market1       | Product1,Product2 |
	  | 1      | Market1       | Product2          |
	  | 2      | Market1       | Product3,Product3 |
	  | 3      | Market1       | Product1          |

	When operator type "spread" named "spread1" created
	  | field             | value   |
	  | inputColumn       | product |
	  | spreadFunction    | csv     |
	  | removeInputColumn | false   |
	And operator "source" output "out" plugged into "spread1" input "in"
	And commit

  Scenario: Can spread CSV column
	When operator type "spread" named "spread2" created
	  | field             | value   |
	  | inputColumn       | product |
	  | spreadFunction    | csv     |
	  | removeInputColumn | false   |
	And operator "source" output "out" plugged into "spread2" input "in"
	And commit
	Then schema for "spread2" is
	  | ~Action     | ~Name       | ~ColumnType |
	  | ColumnAdd   | id          | Int         |
	  | ColumnAdd   | market      | String      |
	  | ColumnAdd   | product     | String      |
	  | ColumnAdd   | product_csv | String      |
	Then data for "spread2" is
	  | ~Action     | id | market  | product_csv |
	  | RowAdd      | 0  | Market1 | Product1    |
	  | RowAdd      | 0  | Market1 | Product2    |
	  | RowAdd      | 1  | Market1 | Product2    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 3  | Market1 | Product1    |
	And commit


  Scenario: Updated row is correctly reflected in spread operator
	When operator type "spread" named "spread2" created
	  | field             | value   |
	  | inputColumn       | product |
	  | spreadFunction    | csv     |
	  | removeInputColumn | false   |
	And operator "source" output "out" plugged into "spread2" input "in"
	And commit
	Then data for "spread2" is
	  | ~Action     | id | market  | product_csv |
	  | RowAdd      | 0  | Market1 | Product1    |
	  | RowAdd      | 0  | Market1 | Product2    |
	  | RowAdd      | 1  | Market1 | Product2    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 3  | Market1 | Product1    |
	When table "source" updated to
	  | id~Int | product~String |
	  | 2      |                |
	And commit
	Then data for "spread2" is
	  | ~Action   | id | market  | product_csv |
	  | RowRemove | 2  | Market1 | Product3    |
	  | RowRemove | 2  | Market1 | Product3    |
	  | RowAdd    | 2  | Market1 |             |
	And commit


  Scenario: Removed row is correctly reflected in spread operator
	When operator type "spread" named "spread2" created
	  | field             | value   |
	  | inputColumn       | product |
	  | spreadFunction    | csv     |
	  | removeInputColumn | false   |
	And operator "source" output "out" plugged into "spread2" input "in"
	And commit
	Then data for "spread2" is
	  | ~Action     | id | market  | product_csv |
	  | RowAdd      | 0  | Market1 | Product1    |
	  | RowAdd      | 0  | Market1 | Product2    |
	  | RowAdd      | 1  | Market1 | Product2    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 3  | Market1 | Product1    |
	When rows "1,2" removed from table "source"
	And commit
	Then data for "spread2" is
	  | ~Action   | id | market  | product_csv |
	  | RowRemove | 2  | Market1 | Product3    |
	  | RowRemove | 2  | Market1 | Product3    |
	  | RowRemove | 1  | Market1 | Product2    |
	And commit


  Scenario: Added row is correctly reflected in spread operator
	When operator type "spread" named "spread2" created
	  | field             | value   |
	  | inputColumn       | product |
	  | spreadFunction    | csv     |
	  | removeInputColumn | false   |
	And operator "source" output "out" plugged into "spread2" input "in"
	And listen for changes on "spread2" output "out"
	And commit
	Then data for "spread2" is
	  | ~Action     | id | market  | product_csv |
	  | RowAdd      | 0  | Market1 | Product1    |
	  | RowAdd      | 0  | Market1 | Product2    |
	  | RowAdd      | 1  | Market1 | Product2    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 2  | Market1 | Product3    |
	  | RowAdd      | 3  | Market1 | Product1    |
	When table "source" updated to
	  | id~Int | product~String |
	  | 4      | foo1,foo2      |
	And commit
	Then data for "spread2" is
	  | ~Action | ~Name | ~ColumnType | ~TEId | id | product_csv |
	  | RowAdd  |       |             | 0     | 4  | foo1        |
	  | RowAdd  |       |             | 1     | 4  | foo2        |

