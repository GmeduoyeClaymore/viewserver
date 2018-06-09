Feature: User registration scenarios

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
	  | ~Action   | ~ColumnType | ~Name                |
	  | ColumnAdd | String      | dimension_userId     |
	  | ColumnAdd | String      | userId               |
	  | ColumnAdd | DateTime    | created              |
	  | ColumnAdd | Date        | dob                  |
	  | ColumnAdd | DateTime    | lastModified         |
	  | ColumnAdd | String      | firstName            |
	  | ColumnAdd | String      | lastName             |
	  | ColumnAdd | String      | contactNo            |
	  | ColumnAdd | Json        | selectedContentTypes |
	  | ColumnAdd | String      | email                |
	  | ColumnAdd | String      | type                 |
	  | ColumnAdd | String      | fcmToken             |
	  | ColumnAdd | Int         | chargePercentage     |
	  | ColumnAdd | Double      | latitude             |
	  | ColumnAdd | Double      | longitude            |
	  | ColumnAdd | Int         | range                |
	  | ColumnAdd | Int         | version              |
	  | ColumnAdd | String      | imageUrl             |
	  | ColumnAdd | Bool        | online               |
	  | ColumnAdd | String      | userStatus           |
	  | ColumnAdd | String      | statusMessage        |
	  | ColumnAdd | Json        | ratings              |
	  | ColumnAdd | Double      | ratingAvg            |
	  | ColumnAdd | Int         | rank                 |
	  | ColumnAdd | Bool        | dimension_online     |
	  | ColumnAdd | Json        | bankAccount          |
	  | ColumnAdd | Json        | paymentCards         |
	  | ColumnAdd | Json        | pendingMessages      |
	  | ColumnAdd | Json        | vehicle              |
	  | ColumnAdd | Json        | relationships        |
	  | ColumnAdd | Json        | deliveryAddress        |
	  | ColumnAdd | String      | relationshipStatus   |
	  | ColumnAdd | String      | userAppStatus        |

	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | contactNo   | distance | email                        | firstName | imageUrl | initiatedByMe | lastName   | online | range | rank | ratingAvg | relationshipStatus | selectedContentTypes                        | statusMessage | type    | userId                                             |
	  | RowAdd  | 07966265016 |          | modestasbricklayer@gmail.com | Modestas  |          |               | BrickLayer | True   | 50    | 0    | -1.0      | UNKNOWN            | {"5":{"selectedProductIds":["BrickLayer"]}} |               | partner | {client2_partnerController_registerPartner_result} |

  Scenario: Location is initially set to primary address location
	When "client2" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | latitude  | longitude | userId                                             |
	  | RowAdd  | 51.4857236 | -0.2123406 | {client2_partnerController_registerPartner_result} |
	Then "client2" controller "userController" action "setLocation" invoked with parameters
	  | Name      | Value |
	  | latitude  | 100   |
	  | longitude | 100   |
	When "client2" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | latitude   | longitude  | userId                                             |
	  | RowAdd  | 100.0 | 100.0 | {client2_partnerController_registerPartner_result} |
	Then "client2" controller "userController" action "setLocation" invoked with parameters
	  | Name      | Value |
	  | latitude  | 0     |
	  | longitude | 0     |
	When "client2" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | latitude  | longitude | userId                                             |
	  | RowAdd  | 51.4857236 | -0.2123406 | {client2_partnerController_registerPartner_result} |

  Scenario: Can see partner for product in userProduct report
	Given "client1" report parameters
	  | Name           | Type    | Value      |
	  | showOutOfRange | String  | true       |
	  | showUnrelated  | String  | true       |
	  | latitude       | Integer | 0          |
	  | longitude      | Integer | 0          |
	  | userId         | Integer | 0          |
	  | maxDistance    | Integer | 0          |
	  | productId      | String  | BrickLayer |
	And "client1" paging from 0 to 100 by "userId" descending
	When "client1" subscribed to report "usersForProduct"
	Then "client1" the following schema is received eventually on report "usersForProduct"
	  | ~Action   | ~ColumnType | ~Name                |
	  | ColumnAdd | String      | relationshipStatus   |
	  | ColumnAdd | String      | relationshipType     |
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
	  | ColumnAdd | Json        | ratings              |
	  | ColumnAdd | Double      | ratingAvg            |
	  | ColumnAdd | Double      | distance             |
	  | ColumnAdd | Int         | rank                 |
	Then "client1" the following data is received eventually on report "usersForProduct"
	  | ~Action | type    | userId                                             |
	  | RowAdd  | partner | 3ABCD24                                            |
	  | RowAdd  | partner | 3ABCD23                                            |
	  | RowAdd  | partner | 3ABCD22                                            |
	  | RowAdd  | partner | {client2_partnerController_registerPartner_result} |

