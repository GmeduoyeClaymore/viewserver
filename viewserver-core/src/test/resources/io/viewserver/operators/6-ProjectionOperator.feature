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
    Then operator "projection1" output "out" is
      | ~Action     | ~Name            | ~ColumnType | ~TEId | market_renamed | notional_renamed | day | id |
      | SchemaReset |                  |             |       |                |                  |     |    |
      | ColumnAdd   | id               | Int         | 0     |                |                  |     |    |
      | ColumnAdd   | market_renamed   | Int         | 1     |                |                  |     |    |
      | ColumnAdd   | day              | Int         | 2     |                |                  |     |    |
      | ColumnAdd   | notional_renamed | Long        | 3     |                |                  |     |    |
      | DataReset   |                  |             |       |                |                  |     |    |
      | RowAdd      | null             | null        | 0     | 10             | 20               | 1   | 0  |
      | RowAdd      | null             | null        | 1     | 10             | 40               | 2   | 1  |
      | RowAdd      | null             | null        | 2     | 30             | 60               | 3   | 2  |
      | RowAdd      | null             | null        | 3     | 40             | 60               | 3   | 3  |
    And commit


  Scenario: Can explicitly include columns
    When operator type "projection" named "projection1" created
      | field    | value  |
      | included | market |
    And operator "source" output "out" plugged into "projection1" input "in"
    Then operator "projection1" output "out" is
      | ~Action     | ~Name  | ~ColumnType | ~TEId | market |
      | SchemaReset |        |             |       |        |
      | ColumnAdd   | market | Int         | 0     |        |
      | DataReset   |        |             |       |        |
      | RowAdd      | null   | null        | 0     | 10     |
      | RowAdd      | null   | null        | 1     | 10     |
      | RowAdd      | null   | null        | 2     | 30     |
      | RowAdd      | null   | null        | 3     | 40     |
    And commit


  Scenario: Can explicitly exclude columns
    When operator type "projection" named "projection1" created
      | field    | value  |
      | excluded | market |
    And operator "source" output "out" plugged into "projection1" input "in"
    Then operator "projection1" output "out" is
      | ~Action     | ~Name    | ~ColumnType | ~TEId | notional | day | id |
      | SchemaReset |          |             |       |          |     |    |
      | ColumnAdd   | id       | Int         | 0     |          |     |    |
      | ColumnAdd   | day      | Int         | 1     |          |     |    |
      | ColumnAdd   | notional | Long        | 2     |          |     |    |
      | DataReset   |          |             |       |          |     |    |
      | RowAdd      | null     | null        | 0     | 20       | 1   | 0  |
      | RowAdd      | null     | null        | 1     | 40       | 2   | 1  |
      | RowAdd      | null     | null        | 2     | 60       | 3   | 2  |
      | RowAdd      | null     | null        | 3     | 60       | 3   | 3  |
    And commit

