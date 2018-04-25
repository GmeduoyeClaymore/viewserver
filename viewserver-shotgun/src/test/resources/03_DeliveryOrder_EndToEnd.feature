Feature: Delivery order scenarios

  Background:
	Given a running shotgun viewserver
	Given a client named "client1" connected to "{client1.url}"
	Given a client named "client2" connected to "{client2.url}"
	Given a client named "client3" connected to "{client3.url}"
	Given a client named "client4" connected to "{client4.url}"
	Given "client1" All data sources are built
	Given "client1" controller "partnerController" action "registerPartner" invoked with data file "allRounder.json"
	Given "client2" controller "partnerController" action "registerPartner" invoked with data file "brickLayerRegistration.json"
	Given "client3" controller "partnerController" action "registerPartner" invoked with data file "plastererRegistration.json"
	Given "client4" controller "partnerController" action "registerPartner" invoked with data file "groundWorkerRegistration.json"


  Scenario: User can create delivery order and see it in their posted order list but not in their request list
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client1" dimension filters
	  | Name                     | Type   | Value           |
	  | dimension_status         | String | ACCEPTED,PLACED |
	  | dimension_customerUserId | String | @userId         |
	When "client1" subscribed to report "customerOrderSummary"
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Name                 | ~ColumnType | partner_email | orderId                                              | totalPrice | partner_latitude | partner_imageUrl | partner_longitude | ~Action     | orderDetails             | partner_ratingAvg | partner_online | partner_userStatus | partner_firstName | partner_lastName | rank | orderContentTypeId | orderLocation                    | partner_statusMessage | status |
	  |                       |             |               |                                                      |            |                  |                  |                   | SchemaReset |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | orderId               | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | status                | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | orderLocation         | Json        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | orderContentTypeId    | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | orderDetails          | Json        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | totalPrice            | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_firstName     | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_lastName      | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_email         | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_latitude      | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_longitude     | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_imageUrl      | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_online        | Bool        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_userStatus    | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_statusMessage | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | partner_ratingAvg     | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | rank                  | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  |                       |             |               |                                                      |            |                  |                  |                   | DataReset   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  |                       |             |               | {client1_deliveryOrderController_createOrder_result} | 0          | 0.0              |                  | 0.0               | RowAdd      | ref://deliveryOrder.json | 0.0               | false          |                    |                   |                  | 0    | 1                  | ref://deliveryOrderLocation.json |                       | PLACED |
	Given "client1" dimension filters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 1       |          |
	  | dimension_customerUserId | String  | @userId | exclude  |
	Given "client1" report parameters
	  | Name             | Type    | Value |
	  | showOutOfRange   | String  | true  |
	  | partnerLatitude  | Integer | 0     |
	  | showUnrelated    | String  | true  |
	  | maxDistance      | Integer | 0     |
	  | partnerLongitude | Integer | 0     |
	When "client1" subscribed to report "orderRequest"
	Then "client1" the following data is received eventually on report "orderRequest"
	  | ~Name                 | ~ColumnType | partner_email | orderId | totalPrice | partner_latitude | partner_imageUrl | partner_longitude | ~Action     | orderDetails | partner_ratingAvg | partner_online | partner_userStatus | partner_firstName | partner_lastName | rank | orderContentTypeId | orderLocation | partner_statusMessage | status |
	  |                       |             |               |         |            |                  |                  |                   | SchemaReset |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | orderId               | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | status                | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | orderLocation         | Json        |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | orderContentTypeId    | Int         |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | orderDetails          | Json        |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | totalPrice            | Int         |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_firstName     | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_lastName      | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_email         | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_latitude      | Double      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_longitude     | Double      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_imageUrl      | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_online        | Bool        |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_userStatus    | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_statusMessage | String      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | partner_ratingAvg     | Double      |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  | rank                  | Int         |               |         |            |                  |                  |                   | ColumnAdd   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |
	  |                       |             |               |         |            |                  |                  |                   | DataReset   |              |                   |                |                    |                   |                  |      |                    |               |                       |        |


  Scenario: Other users can see newly created job in their list
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" dimension filters
	  | Name                    | Type    | Value |
	  | dimension_contentTypeId | Integer | 1     |
	Given "client2" report parameters
	  | Name             | Type    | Value |
	  | showOutOfRange   | String  | true  |
	  | partnerLatitude  | Integer | 0     |
	  | showUnrelated    | String  | true  |
	  | maxDistance      | Integer | 0     |
	  | partnerLongitude | Integer | 0     |
	When "client2" subscribed to report "orderRequest"
	Then "client2" the following schema is received eventually on report "orderRequest"
	  | ~Name                 | ~ColumnType | ~Action     |
	  |                       |             | SchemaReset |
	  | orderId               | String      | ColumnAdd   |
	  | status                | String      | ColumnAdd   |
	  | orderLocation         | Json        | ColumnAdd   |
	  | orderContentTypeId    | Int         | ColumnAdd   |
	  | orderDetails          | Json        | ColumnAdd   |
	  | totalPrice            | Int         | ColumnAdd   |
	  | partner_firstName     | String      | ColumnAdd   |
	  | partner_lastName      | String      | ColumnAdd   |
	  | partner_email         | String      | ColumnAdd   |
	  | partner_latitude      | Double      | ColumnAdd   |
	  | partner_longitude     | Double      | ColumnAdd   |
	  | partner_imageUrl      | String      | ColumnAdd   |
	  | partner_online        | Bool        | ColumnAdd   |
	  | partner_userStatus    | String      | ColumnAdd   |
	  | partner_statusMessage | String      | ColumnAdd   |
	  | partner_ratingAvg     | Double      | ColumnAdd   |
	  | rank                  | Int         | ColumnAdd   |
	Then "client2" the following data is received eventually on report "orderRequest" with rowKey "orderId"
	  | ~Action   | partner_email | orderId                                              | totalPrice | partner_latitude | partner_imageUrl | partner_longitude | orderDetails             | partner_ratingAvg | partner_online | partner_userStatus | partner_firstName | partner_lastName | rank | orderContentTypeId | orderLocation                    | partner_statusMessage | status |
	  | DataReset |               |                                                      |            |                  |                  |                   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |        |
	  | RowAdd    |               | {client1_deliveryOrderController_createOrder_result} | 0          | 0.0              |                  | 0.0               | ref://deliveryOrder.json | 0.0               | false          |                    |                   |                  | 0    | 1                  | ref://deliveryOrderLocation.json |                       | PLACED |

  Scenario: Accepting order causes order to appear in responded order list for partner but still remain in order request list for the rest
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	Given "client2" dimension filters
	  | Name                | Type   | Value     |
	  | dimension_partnerId | String | @userId   |
	  | dimension_status    | String | RESPONDED |
	When "client2" subscribed to report "orderResponses"
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Name                 | ~ColumnType | partner_email | orderId                                              | totalPrice | partner_latitude | partner_imageUrl | partner_longitude | ~Action     | orderDetails             | partner_ratingAvg | partner_online | partner_userStatus | partner_firstName | partner_lastName | rank | orderContentTypeId | orderLocation                    | partner_statusMessage | status    |
	  |                       |             |               |                                                      |            |                  |                  |                   | SchemaReset |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | orderId               | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partnerOrderStatus    | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | orderLocation         | Json        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | orderContentTypeId    | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | totalPrice            | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_firstName     | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_lastName      | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_email         | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_latitude      | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_longitude     | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_imageUrl      | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_online        | Bool        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_userStatus    | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_statusMessage | String      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | partner_ratingAvg     | Double      |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | orderDetails          | Json        |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  | rank                  | Int         |               |                                                      |            |                  |                  |                   | ColumnAdd   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  |                       |             |               |                                                      |            |                  |                  |                   | DataReset   |                          |                   |                |                    |                   |                  |      |                    |                                  |                       |           |
	  |                       |             |               | {client1_deliveryOrderController_createOrder_result} | 0          | 0.0              |                  | 0.0               | RowAdd      | ref://deliveryOrder.json | 0.0               | false          |                    |                   |                  | 0    | 1                  | ref://deliveryOrderLocation.json |                       | RESPONDED |
