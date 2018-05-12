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


  Scenario: User can request friendship with another user and see relationship status
	Given "client1" controller "userController" action "updateRelationship" invoked with parameters
	  | Name               | Value                                              |
	  | targetUserId       | {client2_partnerController_registerPartner_result} |
	  | relationshipStatus | REQUESTED                                          |
	  | relationshipType   | COLLEAGUE                                          |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value                                              |
	  | dimension_userId | String | {client2_partnerController_registerPartner_result} |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | userId                                             | relationshipStatus |
	  | RowAdd  | {client2_partnerController_registerPartner_result} | REQUESTEDBYME          |
	When "client2" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value                                              |
	  | dimension_userId | String | {client1_partnerController_registerPartner_result} |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | userId                                             | relationshipStatus |
	  | RowAdd  | {client1_partnerController_registerPartner_result} | REQUESTED      |
	When "client1" subscribed to report "userRelationships" with parameters
	  | showOutOfRange | String  | true |
	  | showUnrelated  | String  | true |
	  | latitude       | Integer | 0    |
	  | longitude      | Integer | 0    |
	  | userId         | Integer | 0    |
	  | maxDistance    | Integer | 0    |
	Then "client1" the following data is received eventually on report "userRelationships"
	  | ~Action | userId                                             | relationshipStatus |
	  | RowAdd  | {client2_partnerController_registerPartner_result} | REQUESTED      |
	  | RowAdd  | 2BBui                                              |                    |
	  | RowAdd  | 2BBui1                                             |                    |
	  | RowAdd  | 3ABCD22                                            |                    |
	  | RowAdd  | 3ABCD23                                            |                    |
	  | RowAdd  | 3ABCD24                                            |                    |
	  | RowAdd  | {client3_partnerController_registerPartner_result} |                    |
	  | RowAdd  | {client4_partnerController_registerPartner_result} |                    |




