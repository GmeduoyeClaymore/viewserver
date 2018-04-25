Feature: Filter operator fixture

  Background:
	Given table named "source" with data
	  | id~Int | market~String | product~String | time~Long            | code~Int | tied~Bool | byteVal~Byte | shortVal~Short | floatVal~Float | doubleVal~Double      |
	  | 0      | Market1       | Product1       | -9223372036854775808 | 11111    | true      | -127         | -32768         | 3.87847593     | 34.238476924128584    |
	  | 1      | Market1       | Product2       | 9223372036854775806  | 22222    | false     | 127          | 32767          | -4.2348973249  | -32.45682768726487234 |
	  | 2      | Market1       | Product3       | -822337203477580     | 33333    | true      | 10           | 234            | 5.187398198    | 4444.55635009754445   |
	  | 3      | Market1       | Product1       | 7223372036775806     | 44444    | false     | 0            | 23230          | -35.187398198  | -54874.24768768748726 |
	  | 4      | Market1       | Product4       | 8223372036775805     | 55555    | true      | 120          | -6540          | 65.642348198   | 478.345               |
	  | 5      | Market2       | Product1       | 7223372036775807     | 66666    | false     | -20          | 304            | 55.818734698   | -33567.24768768748726 |
	  | 6      | Market2       | Product2       | -6223372054775807    | 77777    | true      | -30          | -404           | 54.2187398198  | -5683.6456645688      |
	  | 7      | Market2       | Product3       | 5223372034775807     | 88888    | false     | 29           | -503           | -54.2198       | 33679.24768768748726  |
	  | 8      | Market2       | Product4       | 4223372054775807     | 99999    | true      | 23           | 0              | -565.18198     | 245.88999538835       |
	  | 9      | Market2       | Product1       | 123376854775807      | 101010   | false     | -120         | 770            | 98.1452268198  | 2567.56832224588649   |
	When operator type "filter" named "defaultFilter1" created
	  | field            | value         |
	  | filterMode       | Filter        |
	  | filterExpression | code == 22222 |
	And operator "source" output "out" plugged into "defaultFilter1" input "in"
	And listen for changes on "defaultFilter1" output "out"
	And commit

  Scenario: Can filter by datatype String
	When operator type "filter" named "stringFilter1" created
	  | field            | value               |
	  | filterMode       | Filter              |
	  | filterExpression | market == "Market1" |
	And operator "source" output "out" plugged into "stringFilter1" input "in"
	And listen for changes on "stringFilter1" output "out"
	And commit
	Then schema for "stringFilter1" is
	  | ~Action   | ~Name     | ~ColumnType |
	  | ColumnAdd | id        | Int         |
	  | ColumnAdd | market    | String      |
	  | ColumnAdd | product   | String      |
	  | ColumnAdd | time      | Long        |
	  | ColumnAdd | code      | Int         |
	  | ColumnAdd | tied      | Bool        |
	  | ColumnAdd | byteVal   | Byte        |
	  | ColumnAdd | shortVal  | Short       |
	  | ColumnAdd | floatVal  | Float       |
	  | ColumnAdd | doubleVal | Double      |
	Then data for "stringFilter1" is
	  | ~Action | doubleVal           | byteVal | code  | floatVal   | id | market  | product  | shortVal | tied  | time                 |
	  | RowAdd  | 34.238476924128584  | -127    | 11111 | 3.878476   | 0  | Market1 | Product1 | -32768   | true  | -9223372036854775808 |
	  | RowAdd  | -32.45682768726487  | 127     | 22222 | -4.234897  | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806  |
	  | RowAdd  | 4444.556350097544   | 10      | 33333 | 5.1873984  | 2  | Market1 | Product3 | 234      | true  | -822337203477580     |
	  | RowAdd  | -54874.247687687486 | 0       | 44444 | -35.187397 | 3  | Market1 | Product1 | 23230    | false | 7223372036775806     |
	  | RowAdd  | 478.345             | 120     | 55555 | 65.64235   | 4  | Market1 | Product4 | -6540    | true  | 8223372036775805     |

  Scenario: Can filter by datatype Long
	When operator type "filter" named "longFilter1" created
	  | field            | value                        |
	  | filterMode       | Filter                       |
	  | filterExpression | time == 9223372036854775806l |
	And operator "source" output "out" plugged into "longFilter1" input "in"
	And listen for changes on "longFilter1" output "out"
	And commit
	Then data for "longFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |

  Scenario: Can filter by datatype Bool
	When operator type "filter" named "boolFilter1" created
	  | field            | value        |
	  | filterMode       | Filter       |
	  | filterExpression | tied == true |
	And operator "source" output "out" plugged into "boolFilter1" input "in"
	And listen for changes on "boolFilter1" output "out"
	And commit
	Then data for "boolFilter1" is
	  | ~Action | doubleVal          | byteVal | code  | floatVal  | id | market  | product  | shortVal | tied | time                 |
	  | RowAdd  | 34.238476924128584 | -127    | 11111 | 3.878476  | 0  | Market1 | Product1 | -32768   | true | -9223372036854775808 |
	  | RowAdd  | 4444.556350097544  | 10      | 33333 | 5.1873984 | 2  | Market1 | Product3 | 234      | true | -822337203477580     |
	  | RowAdd  | 478.345            | 120     | 55555 | 65.64235  | 4  | Market1 | Product4 | -6540    | true | 8223372036775805     |
	  | RowAdd  | -5683.6456645688   | -30     | 77777 | 54.21874  | 6  | Market2 | Product2 | -404     | true | -6223372054775807    |
	  | RowAdd  | 245.88999538835    | 23      | 99999 | -565.182  | 8  | Market2 | Product4 | 0        | true | 4223372054775807     |

  Scenario: Can filter by datatype Float
	When operator type "filter" named "floatFilter1" created
	  | field            | value                      |
	  | filterMode       | Filter                     |
	  | filterExpression | floatVal == -4.2348973249f |
	And operator "source" output "out" plugged into "floatFilter1" input "in"
	And listen for changes on "floatFilter1" output "out"
	And commit
	Then data for "floatFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |

  Scenario: Can filter by datatype Double
	When operator type "filter" named "doubleFilter1" created
	  | field            | value                               |
	  | filterMode       | Filter                              |
	  | filterExpression | doubleVal == -32.45682768726487234d |
	And operator "source" output "out" plugged into "doubleFilter1" input "in"
	And listen for changes on "doubleFilter1" output "out"
	And commit
	Then data for "doubleFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |

  Scenario: Can remove rows
	When listen for changes on "defaultFilter1" output "out"
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
	And commit
	When rows "1,2" removed from table "source"
	And commit
	Then data for "defaultFilter1" is
	  | ~Action   | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowRemove | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |

  Scenario: Can update rows∂
	When listen for changes on "defaultFilter1" output "out"
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
	And commit
	When table "source" updated to
	  | id~Int | code~Int |
	  | 1      | 300      |
	  | 0      | 22222    |
	And commit
	Then data for "defaultFilter1" is
	  | ~Action   | id | market  | product  | time                 |
	  | RowRemove | 1  |         |          |                      |
	  | RowAdd    | 0  | Market1 | Product1 | -9223372036854775808 |

  Scenario: Can reset table
	When listen for changes on "defaultFilter1" output "out"
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
	And commit
	When table "source" reset
	Then data for "defaultFilter1" is
	  | ~Action | product | time | market |
	And commit

  Scenario: Can update filter expression
	When listen for changes on "defaultFilter1" output "out"
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
	And commit
	Then operator "defaultFilter1" of type "filter" is configured to
	  | field            | value         |
	  | filterMode       | Filter        |
	  | filterExpression | code == 33333 |
	And commit
	Then data for "defaultFilter1" is
	  | ~Action | id | market  | product  | time             |
	  | RowAdd  | 2  | Market1 | Product3 | -822337203477580 |

  Scenario: When filtered column removed filter is un applied
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
	  | RowAdd  | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
	And commit
	When columns "code,product" removed from table "source"
	And commit
	Then data for "defaultFilter1" is
	  | ~Action | byteVal | code | doubleVal           | floatVal   | id | market  | product | shortVal | tied  | time                 |
	  | RowAdd  | -127    |      | 34.238476924128584  | 3.878476   | 0  | Market1 |         | -32768   | true  | -9223372036854775808 |
	  | RowAdd  | 127     |      | -32.45682768726487  | -4.234897  | 1  | Market1 |         | 32767    | false | 9223372036854775806  |
	  | RowAdd  | 10      |      | 4444.556350097544   | 5.1873984  | 2  | Market1 |         | 234      | true  | -822337203477580     |
	  | RowAdd  | 0       |      | -54874.247687687486 | -35.187397 | 3  | Market1 |         | 23230    | false | 7223372036775806     |
	  | RowAdd  | 120     |      | 478.345             | 65.64235   | 4  | Market1 |         | -6540    | true  | 8223372036775805     |
	  | RowAdd  | -20     |      | -33567.247687687486 | 55.818733  | 5  | Market2 |         | 304      | false | 7223372036775807     |
	  | RowAdd  | -30     |      | -5683.6456645688    | 54.21874   | 6  | Market2 |         | -404     | true  | -6223372054775807    |
	  | RowAdd  | 29      |      | 33679.247687687486  | -54.2198   | 7  | Market2 |         | -503     | false | 5223372034775807     |
	  | RowAdd  | 23      |      | 245.88999538835     | -565.182   | 8  | Market2 |         | 0        | true  | 4223372054775807     |
	  | RowAdd  | -120    |      | 2567.5683222458865  | 98.145226  | 9  | Market2 |         | 770      | false | 123376854775807      |

  #currently if a filtered column is removed and then re-added the filter is not re-applied maybe it should be ?????
#  Scenario: When filtered column re-added filter is re-applied
#    Then operator "filter1" output "out" is
#      | ~Action     | ~Name   | ~TEId | id | ~ContentType | product | time | market |
#      | SchemaReset |         |       |    |             |         |      |        |
#      | ColumnAdd   | id      | 0     |    | Int         |         |      |        |
#      | ColumnAdd   | market  | 1     |    | String      |         |      |        |
#      | ColumnAdd   | product | 2     |    | String      |         |      |        |
#      | ColumnAdd   | time    | 3     |    | Long        |         |      |        |
#      | DataReset   |         |       |    |             |         |      |        |
#      | RowAdd      |         | 0     | 1  |             | p2      | 200  | Mark1  |
#    And commit
#    When columns "time,product" removed from table "source"
#    Then operator "filter1" output "out" is
#      | ~Action      | ~TEId | ~Name   | id | ~ContentType | market |
#      | ColumnRemove | 3     | time    | 1  | Long        |        |
#      | ColumnRemove | 2     | product | 2  | String      |        |
#      | DataReset    |       |         |    |             |        |
#      | RowAdd       | 0     |         | 1  |             | Mark1  |
#      | RowAdd       | 1     |         | 0  |             | Mark1  |
#      | RowAdd       | 2     |         | 2  |             | Mark1  |
#    And commit
#    When columns added to table "source"
#      | ~Name   |  | ~ContentType |
#      | product |  | String      |
#      | time    |  | Long        |
#    And table "source" updated to
#      | id~Int | time~Long |
#      | 0      | 200       |
#      | 1      | 300       |
#      | 2      | 400       |
#    Then operator "filter1" output "out" is
#      | ~Action   | ~TEId | ~Name   | id | ~ContentType | market | product | time  |
#      | ColumnAdd | 3     | time    | 1  | Long        |        |         |       |
#      | ColumnAdd | 2     | product | 2  | String      |        |         |       |
#      | DataReset |       |         |    |             |        |         |       |
#      | RowAdd    | 0     | 0       | 1  |             | p2     | 200     | Mark1 |
#    And commit





