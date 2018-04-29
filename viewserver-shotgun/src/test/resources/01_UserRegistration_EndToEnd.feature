Feature: User relationship scenarios

  Background:
	Given a running shotgun viewserver
	Given a client named "client1" connected to "{client1.url}"
	Given a client named "client2" connected to "{client2.url}"
	Given a client named "client3" connected to "{client3.url}"
	Given a client named "client4" connected to "{client4.url}"
	Given keyColumn is "userId"
	Given "client1" All data sources are built
	Given "client1" controller "partnerController" action "registerPartner" invoked with data file "json/users/allRounder.json"
	Given "client2" controller "partnerController" action "registerPartner" invoked with data file "json/users/brickLayerRegistration.json"
	Given "client3" controller "partnerController" action "registerPartner" invoked with data file "json/users/plastererRegistration.json"
	Given "client4" controller "partnerController" action "registerPartner" invoked with data file "json/users/groundWorkerRegistration.json"


  Scenario: Newly registered users can see themselves
	Given "client2" dimension filters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	When "client2" subscribed to report "userReport"
	Then "client2" the following schema is received eventually on report "userReport"
	  | ~Action   | ~ColumnType | ~Name                 |
	  | ColumnAdd | String      | dimension_userId      |
	  | ColumnAdd | String      | userId                |
	  | ColumnAdd | DateTime    | created               |
	  | ColumnAdd | Date        | dob                   |
	  | ColumnAdd | DateTime    | lastModified          |
	  | ColumnAdd | String      | firstName             |
	  | ColumnAdd | String      | lastName              |
	  | ColumnAdd | String      | contactNo             |
	  | ColumnAdd | Json        | selectedContentTypes  |
	  | ColumnAdd | String      | email                 |
	  | ColumnAdd | String      | type                  |
	  | ColumnAdd | String      | stripeDefaultSourceId |
	  | ColumnAdd | String      | fcmToken              |
	  | ColumnAdd | Int         | chargePercentage      |
	  | ColumnAdd | Double      | latitude              |
	  | ColumnAdd | Double      | longitude             |
	  | ColumnAdd | Int         | range                 |
	  | ColumnAdd | String      | imageUrl              |
	  | ColumnAdd | Bool        | online                |
	  | ColumnAdd | String      | userStatus            |
	  | ColumnAdd | String      | statusMessage         |
	  | ColumnAdd | String      | ratingGroupBy_userId  |
	  | ColumnAdd | Int         | count                 |
	  | ColumnAdd | Double      | ratingAvg             |
	  | ColumnAdd | Int         | rank                  |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | contactNo    | distance | email                        | firstName | imageUrl | initiatedByMe | lastName   | latitude | longitude | market | notional | online | range | rank | ratingAvg | relationshipStatus | selectedContentTypes                        | statusMessage | type    | userId                                             |
	  | RowAdd  | 447966265016 |          | modestasbricklayer@gmail.com | Modestas  |          |               | BrickLayer | 0.0      | 0.0       |        |          | True   | 50    | 0    | 0.0       |                    | {"5":{"selectedProductIds":["BrickLayer"]}} |               | partner | {client2_partnerController_registerPartner_result} |

  Scenario: Newly registered users can be seen by each other

  Scenario: Can see partner for product in userProduct report
	Given "client1" report parameters
	  | Name           | Type    | Value |
	  | showOutOfRange | String  | false |
	  | showUnrelated  | String  | true  |
	  | latitude       | Integer | 0     |
	  | longitude      | Integer | 0     |
	  | userId         | Integer | 0     |
	  | maxDistance    | Integer | 0     |
	Given "client1" dimension filters
	  | Name                | Type   | Value      |
	  | dimension_productId | String | BrickLayer |
	And "client1" paging from 0 to 100 by "userId" descending
	When "client1" subscribed to report "usersForProductAll"
	Then "client1" the following schema is received eventually on report "usersForProductAll"
	  | ~Action   | ~ColumnType | ~Name                |
	  | ColumnAdd | String      | relationshipStatus   |
	  | ColumnAdd | Bool        | initiatedByMe        |
	  | ColumnAdd | String      | userId               |
	  | ColumnAdd | String      | firstName            |
	  | ColumnAdd | String      | lastName             |
	  | ColumnAdd | String      | contactNo            |
	  | ColumnAdd | Json        | selectedContentTypes |
	  | ColumnAdd | String      | email                |
	  | ColumnAdd | String      | type                 |
	  | ColumnAdd | Double      | latitude             |
	  | ColumnAdd | Double      | longitude            |
	  | ColumnAdd | Int         | range                |
	  | ColumnAdd | String      | imageUrl             |
	  | ColumnAdd | Bool        | online               |
	  | ColumnAdd | String      | statusMessage        |
	  | ColumnAdd | Double      | ratingAvg            |
	  | ColumnAdd | Double      | distance             |
	  | ColumnAdd | Int         | rank                 |
	Then "client1" the following data is received eventually on report "usersForProductAll"
	  | ~Action | contactNo    | distance | email                        | firstName | id | imageUrl | initiatedByMe | lastName   | latitude | longitude | market | notional | online | range | rank | ratingAvg | relationshipStatus | selectedContentTypes                        | statusMessage | type    | userId                                             |
	  | RowAdd  | 447966265016 | 0.0      | modestasbricklayer@gmail.com | Modestas  |    |          | Null          | BrickLayer | 0.0      | 0.0       |        |          | true   | 50    | 0    | 0.0       |                    | {"5":{"selectedProductIds":["BrickLayer"]}} |               | partner | {client2_partnerController_registerPartner_result} |