Feature: Multiple operators

  Background:
    Given table named "source1" with data
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


  Scenario: Multiple filter operators
    When operator type "filter" named "Filter1" created
      | field            | value               |
      | filterMode       | Filter              |
      | filterExpression | market == "Market1" |
    And operator type "filter" named "Filter2" created
      | field            | value                 |
      | filterMode       | Filter                |
      | filterExpression | product == "Product1" |
    And operator type "filter" named "Filter3" created
      | field            | value            |
      | filterMode       | Filter           |
      | filterExpression | shortVal > 20000 |
    And operator "source1" output "out" plugged into "Filter1" input "in"
    And operator "Filter1" output "out" plugged into "Filter2" input "in"
    And operator "Filter2" output "out" plugged into "Filter3" input "in"
    And listen for changes on "Filter3" output "out"
    And commit
    Then operator "Filter3" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | calcCol1 | code  | doubleVal           | floatVal   | id | market  | product  | shortVal | tied  | time             |
      | SchemaReset |           |             |       |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | id        | Int         | 0     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | market    | String      | 1     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | product   | String      | 2     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | time      | Long        | 3     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | code      | Int         | 4     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | tied      | Bool        | 5     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | byteVal   | Byte        | 6     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | shortVal  | Short       | 7     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | floatVal  | Float       | 8     |         |          |       |                     |            |    |         |          |          |       |                  |
      | ColumnAdd   | doubleVal | Double      | 9     |         |          |       |                     |            |    |         |          |          |       |                  |
      | DataReset   |           |             |       |         |          |       |                     |            |    |         |          |          |       |                  |
      | RowAdd      |           |             | 0     | 0       |          | 44444 | -54874.247687687486 | -35.187397 | 3  | Market1 | Product1 | 23230    | false | 7223372036775806 |
    And commit

  Scenario: Multiple CalcCol operator output
    When operator type "calc" named "CalculatedCol1" created
      | field           | value       |
      | calcColumnName1 | calcCol1    |
      | calcExpression1 | code * code |
  # And operator type "calc" named "CalcCol2" created
  #   | field           | value           |
  #   | calcColumnName1 | calcCol2        |
  #   | calcExpression1 | calcCol1 + code |
  # And operator type "calc" named "CalcCol3" created
  #   | field           | value           |
  #   | calcColumnName1 | calcCol3        |
  #   | calcExpression1 | calcCol2 + code |
    And operator "source1" output "out" plugged into "CalculatedCol1" input "in"
  #  And operator "CalcCol1" output "out" plugged into "CalcCol2" input "in"
  #  And operator "CalcCol2" output "out" plugged into "CalcCol3" input "in"
    And listen for changes on "CalculatedCol1" output "out"
    And commit
    Then operator "CalculatedCol1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | calcCol1    | byteVal | code   | doubleVal           | floatVal   | id | market  | product  | shortVal | tied  | time                 |
      | SchemaReset |           |             |       |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | id        | Int         | 0     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |             |         |        |                     |            |    |         |          |          |       |                      |
      | ColumnAdd   | calcCol1  | Long        | 10    |             |         |        |                     |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |             |         |        |                     |            |    |         |          |          |       |                      |
      | RowAdd      |           |             | 0     | 123454321   | -127    | 11111  | 34.238476924128584  | 3.878476   | 0  | Market1 | Product1 | -32768   | true  | -9223372036854775808 |
      | RowAdd      |           |             | 1     | 493817284   | 127     | 22222  | -32.45682768726487  | -4.234897  | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806  |
      | RowAdd      |           |             | 2     | 1111088889  | 10      | 33333  | 4444.556350097544   | 5.1873984  | 2  | Market1 | Product3 | 234      | true  | -822337203477580     |
      | RowAdd      |           |             | 3     | 1975269136  | 0       | 44444  | -54874.247687687486 | -35.187397 | 3  | Market1 | Product1 | 23230    | false | 7223372036775806     |
      | RowAdd      |           |             | 4     | 3086358025  | 120     | 55555  | 478.345             | 65.64235   | 4  | Market1 | Product4 | -6540    | true  | 8223372036775805     |
      | RowAdd      |           |             | 5     | 4444355556  | -20     | 66666  | -33567.247687687486 | 55.818733  | 5  | Market2 | Product1 | 304      | false | 7223372036775807     |
      | RowAdd      |           |             | 6     | 6049261729  | -30     | 77777  | -5683.6456645688    | 54.21874   | 6  | Market2 | Product2 | -404     | true  | -6223372054775807    |
      | RowAdd      |           |             | 7     | 7901076544  | 29      | 88888  | 33679.247687687486  | -54.2198   | 7  | Market2 | Product3 | -503     | false | 5223372034775807     |
      | RowAdd      |           |             | 8     | 9999800001  | 23      | 99999  | 245.88999538835     | -565.182   | 8  | Market2 | Product4 | 0        | true  | 4223372054775807     |
      | RowAdd      |           |             | 9     | 10203020100 | -120    | 101010 | 2567.5683222458865  | 98.145226  | 9  | Market2 | Product1 | 770      | false | 123376854775807      |
    And commit