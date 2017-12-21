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
    Then operator "stringFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | doubleVal           | byteVal | code  | floatVal   | id | market  | product  | shortVal | tied  | time                 |
      | SchemaReset |           |             |       |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |                     |         |       |            |    |         |          |          |       |                      |
      | RowAdd      |           |             | 0     | 34.238476924128584  | -127    | 11111 | 3.878476   | 0  | Market1 | Product1 | -32768   | true  | -9223372036854775808 |
      | RowAdd      |           |             | 1     | -32.45682768726487  | 127     | 22222 | -4.234897  | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806  |
      | RowAdd      |           |             | 2     | 4444.556350097544   | 10      | 33333 | 5.1873984  | 2  | Market1 | Product3 | 234      | true  | -822337203477580     |
      | RowAdd      |           |             | 3     | -54874.247687687486 | 0       | 44444 | -35.187397 | 3  | Market1 | Product1 | 23230    | false | 7223372036775806     |
      | RowAdd      |           |             | 4     | 478.345             | 120     | 55555 | 65.64235   | 4  | Market1 | Product4 | -6540    | true  | 8223372036775805     |
    And commit

  Scenario: Can filter by datatype Long
    When operator type "filter" named "longFilter1" created
      | field            | value                        |
      | filterMode       | Filter                       |
      | filterExpression | time == 9223372036854775806l |
    And operator "source" output "out" plugged into "longFilter1" input "in"
    And listen for changes on "longFilter1" output "out"
    And commit
    Then operator "longFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit

  Scenario: Can filter by datatype Bool
    When operator type "filter" named "boolFilter1" created
      | field            | value        |
      | filterMode       | Filter       |
      | filterExpression | tied == true |
    And operator "source" output "out" plugged into "boolFilter1" input "in"
    And listen for changes on "boolFilter1" output "out"
    And commit
    Then operator "boolFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | doubleVal          | byteVal | code  | floatVal  | id | market  | product  | shortVal | tied | time                 |
      | SchemaReset |           |             |       |                    |         |       |           |    |         |          |          |      |                      |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |                    |         |       |           |    |         |          |          |      |                      |
      | RowAdd      |           |             | 0     | 34.238476924128584 | -127    | 11111 | 3.878476  | 0  | Market1 | Product1 | -32768   | true | -9223372036854775808 |
      | RowAdd      |           |             | 1     | 4444.556350097544  | 10      | 33333 | 5.1873984 | 2  | Market1 | Product3 | 234      | true | -822337203477580     |
      | RowAdd      |           |             | 2     | 478.345            | 120     | 55555 | 65.64235  | 4  | Market1 | Product4 | -6540    | true | 8223372036775805     |
      | RowAdd      |           |             | 3     | -5683.6456645688   | -30     | 77777 | 54.21874  | 6  | Market2 | Product2 | -404     | true | -6223372054775807    |
      | RowAdd      |           |             | 4     | 245.88999538835    | 23      | 99999 | -565.182  | 8  | Market2 | Product4 | 0        | true | 4223372054775807     |
    And commit

  Scenario: Can filter by datatype Float
    When operator type "filter" named "floatFilter1" created
      | field            | value                      |
      | filterMode       | Filter                     |
      | filterExpression | floatVal == -4.2348973249f |
    And operator "source" output "out" plugged into "floatFilter1" input "in"
    And listen for changes on "floatFilter1" output "out"
    And commit
    Then operator "floatFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit

  Scenario: Can filter by datatype Double
    When operator type "filter" named "doubleFilter1" created
      | field            | value                               |
      | filterMode       | Filter                              |
      | filterExpression | doubleVal == -32.45682768726487234d |
    And operator "source" output "out" plugged into "doubleFilter1" input "in"
    And listen for changes on "doubleFilter1" output "out"
    And commit
    Then operator "doubleFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit

  Scenario: Can remove rows
    Then operator "defaultFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit
    When rows "1,2" removed from table "source"
    Then operator "defaultFilter1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | byteVal | code | doubleVal | floatVal | id | market | product | shortVal | tied | time |
      | RowRemove |       |             | 0     |         |      |           |          |    |        |         |          |      |      |
    And commit

  Scenario: Can update rows
    Then operator "defaultFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit
    When table "source" updated to
      | id~Int | code~Int |
      | 1      | 300      |
      | 0      | 22222    |
    Then operator "defaultFilter1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | id | market  | product  | time                 |
      | RowRemove |       |             | 0     |    |         |          |                      |
      | RowAdd    |       |             | 1     | 0  | Market1 | Product1 | -9223372036854775808 |
    And commit

  Scenario: Can reset table
    Then operator "defaultFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit
    When table "source" reset
    Then operator "defaultFilter1" output "out" is
      | ~Action | ~TEId | id | ~ColumnType | product | time | market |
    And commit

  Scenario: Can update filter expression
    Then operator "defaultFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit
    Then operator "defaultFilter1" of type "filter" is configured to
      | field            | value         |
      | filterMode       | Filter        |
      | filterExpression | code == 33333 |
    Then operator "defaultFilter1" output "out" is
      | ~Action   | ~Name | ~ColumnType | ~TEId | id | market  | product  | time             |
      | DataReset |       |             |       |    |         |          |                  |
      | RowAdd    |       |             | 0     | 2  | Market1 | Product3 | -822337203477580 |
    And commit

  Scenario: When filtered column removed filter is un applied
    Then operator "defaultFilter1" output "out" is
      | ~Action     | ~Name     | ~ColumnType | ~TEId | byteVal | code  | doubleVal          | floatVal  | id | market  | product  | shortVal | tied  | time                |
      | SchemaReset |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | ColumnAdd   | id        | Int         | 0     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | market    | String      | 1     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | product   | String      | 2     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | time      | Long        | 3     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | code      | Int         | 4     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | tied      | Bool        | 5     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | byteVal   | Byte        | 6     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | shortVal  | Short       | 7     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | floatVal  | Float       | 8     |                     |         |       |            |    |         |          |          |       |                      |
      | ColumnAdd   | doubleVal | Double      | 9     |                     |         |       |            |    |         |          |          |       |                      |
      | DataReset   |           |             |       |         |       |                    |           |    |         |          |          |       |                     |
      | RowAdd      |           |             | 0     | 127     | 22222 | -32.45682768726487 | -4.234897 | 1  | Market1 | Product2 | 32767    | false | 9223372036854775806 |
    And commit
    When columns "code,product" removed from table "source"
    Then operator "defaultFilter1" output "out" is
      | ~Action      | ~Name   | ~ColumnType | ~TEId | byteVal | code | doubleVal           | floatVal   | id | market  | product | shortVal | tied  | time                 |
      | ColumnRemove | code    | Int         | 4     |         |      |                     |            |    |         |         |          |       |                      |
      | ColumnRemove | product | String      | 2     |         |      |                     |            |    |         |         |          |       |                      |
      | DataReset    |         |             |       |         |      |                     |            |    |         |         |          |       |                      |
      | RowAdd       |         |             | 0     | -127    |      | 34.238476924128584  | 3.878476   | 0  | Market1 |         | -32768   | true  | -9223372036854775808 |
      | RowAdd       |         |             | 1     | 127     |      | -32.45682768726487  | -4.234897  | 1  | Market1 |         | 32767    | false | 9223372036854775806  |
      | RowAdd       |         |             | 2     | 10      |      | 4444.556350097544   | 5.1873984  | 2  | Market1 |         | 234      | true  | -822337203477580     |
      | RowAdd       |         |             | 3     | 0       |      | -54874.247687687486 | -35.187397 | 3  | Market1 |         | 23230    | false | 7223372036775806     |
      | RowAdd       |         |             | 4     | 120     |      | 478.345             | 65.64235   | 4  | Market1 |         | -6540    | true  | 8223372036775805     |
      | RowAdd       |         |             | 5     | -20     |      | -33567.247687687486 | 55.818733  | 5  | Market2 |         | 304      | false | 7223372036775807     |
      | RowAdd       |         |             | 6     | -30     |      | -5683.6456645688    | 54.21874   | 6  | Market2 |         | -404     | true  | -6223372054775807    |
      | RowAdd       |         |             | 7     | 29      |      | 33679.247687687486  | -54.2198   | 7  | Market2 |         | -503     | false | 5223372034775807     |
      | RowAdd       |         |             | 8     | 23      |      | 245.88999538835     | -565.182   | 8  | Market2 |         | 0        | true  | 4223372054775807     |
      | RowAdd       |         |             | 9     | -120    |      | 2567.5683222458865  | 98.145226  | 9  | Market2 |         | 770      | false | 123376854775807      |
    And commit

  #currently if a filtered column is removed and then re-added the filter is not re-applied maybe it should be ?????
#  Scenario: When filtered column re-added filter is re-applied
#    Then operator "filter1" output "out" is
#      | ~Action     | ~Name   | ~TEId | id | ~ColumnType | product | time | market |
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
#      | ~Action      | ~TEId | ~Name   | id | ~ColumnType | market |
#      | ColumnRemove | 3     | time    | 1  | Long        |        |
#      | ColumnRemove | 2     | product | 2  | String      |        |
#      | DataReset    |       |         |    |             |        |
#      | RowAdd       | 0     |         | 1  |             | Mark1  |
#      | RowAdd       | 1     |         | 0  |             | Mark1  |
#      | RowAdd       | 2     |         | 2  |             | Mark1  |
#    And commit
#    When columns added to table "source"
#      | ~Name   |  | ~ColumnType |
#      | product |  | String      |
#      | time    |  | Long        |
#    And table "source" updated to
#      | id~Int | time~Long |
#      | 0      | 200       |
#      | 1      | 300       |
#      | 2      | 400       |
#    Then operator "filter1" output "out" is
#      | ~Action   | ~TEId | ~Name   | id | ~ColumnType | market | product | time  |
#      | ColumnAdd | 3     | time    | 1  | Long        |        |         |       |
#      | ColumnAdd | 2     | product | 2  | String      |        |         |       |
#      | DataReset |       |         |    |             |        |         |       |
#      | RowAdd    | 0     | 0       | 1  |             | p2     | 200     | Mark1 |
#    And commit





