Feature: User realtionship scenarios

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
	  | RowAdd  | {client2_partnerController_registerPartner_result} | REQUESTEDBYME      |
	When "client2" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value                                              |
	  | dimension_userId | String | {client1_partnerController_registerPartner_result} |
	Then "client2" the following data is received eventually on report "userReport"
	  | ~Action | userId                                             | relationshipStatus |
	  | RowAdd  | {client1_partnerController_registerPartner_result} | REQUESTED          |
	When "client1" subscribed to report "userRelationships" with parameters
	  | showOutOfRange | String  | true |
	  | showUnrelated  | String  | true |
	  | latitude       | Integer | 0    |
	  | longitude      | Integer | 0    |
	  | userId         | Integer | 0    |
	  | maxDistance    | Integer | 0    |
	Then "client1" the following data is received eventually on report "userRelationships"
	  | ~Action | userId                                             | relationshipStatus |
	  | RowAdd  | {client2_partnerController_registerPartner_result} | REQUESTED          |
	  | RowAdd  | 3ABCD22                                            |                    |
	  | RowAdd  | 3ABCD23                                            |                    |
	  | RowAdd  | 3ABCD24                                            |                    |
	  | RowAdd  | {client3_partnerController_registerPartner_result} |                    |
	  | RowAdd  | {client4_partnerController_registerPartner_result} |                    |


  Scenario: Blocking user causes your jobs to dissapear from that users lists
	Given keyColumn is "orderId"
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "json/orders/createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" subscribed to report "orderRequest" with parameters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 1       |          |
	  | dimension_customerUserId | String  | @userId | exclude  |
	  | dimension_status         | String  | PLACED  |          |
	  | showOutOfRange           | String  | true    |          |
	  | partnerLatitude          | Integer | 0       |          |
	  | showUnrelated            | String  | true    |          |
	  | maxDistance              | Integer | 0       |          |
	  | partnerLongitude         | Integer | 0       |          |
	Then "client2" the following data is received eventually on report "orderRequest"
	  | ~Action | orderId                                              | orderDetails                         | orderLocation                                | status |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | ref://json/orders/deliveryOrder.json | ref://json/orders/deliveryOrderLocation.json | PLACED |
	Given "client1" controller "userController" action "updateRelationship" invoked with parameters
	  | Name               | Value                                              |
	  | targetUserId       | {client2_partnerController_registerPartner_result} |
	  | relationshipStatus | BLOCKED                                            |
	  | relationshipType   | COLLEAGUE                                          |
	Then "client2" the following data is received eventually on report "orderRequest"
	  | ~Action   | orderId                                              |
	  | RowAdd    | {client1_deliveryOrderController_createOrder_result} |
	  | RowRemove | {client1_deliveryOrderController_createOrder_result} |


  Scenario: Blocking user causes no notifications for jobs
	Given keyColumn is "orderId"
	Given "client1" controller "userController" action "updateRelationship" invoked with parameters
	  | Name               | Value                                              |
	  | targetUserId       | {client2_partnerController_registerPartner_result} |
	  | relationshipStatus | BLOCKED                                            |
	  | relationshipType   | COLLEAGUE                                          |
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "json/orders/createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" subscribed to report "orderRequest" with parameters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 1       |          |
	  | dimension_customerUserId | String  | @userId | exclude  |
	  | dimension_status         | String  | PLACED  |          |
	  | showOutOfRange           | String  | true    |          |
	  | partnerLatitude          | Integer | 0       |          |
	  | showUnrelated            | String  | true    |          |
	  | maxDistance              | Integer | 0       |          |
	  | partnerLongitude         | Integer | 0       |          |
	Then "client2" the following data is received terminally on report "orderRequest"
	  | ~Action | orderId |
	Then "client2" the following notifications are received terminally
	  | ~Action | fromUserId                                         | message.title          |
	  | RowAdd  | {client1_partnerController_registerPartner_result} | Shotgun friend request |

