Feature: Delivery order scenarios

  Background:
	Given a running shotgun viewserver
	Given a client named "client1" connected to "{client1.url}"
	Given a client named "client2" connected to "{client2.url}"
	Given a client named "client3" connected to "{client3.url}"
	Given a client named "client4" connected to "{client4.url}"
	Given keyColumn is "orderId"
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
	Then "client1" the following schema is received eventually on report "customerOrderSummary"
	  | ~Action   | ~Name                 | ~ColumnType |
	  | ColumnAdd | orderId               | String      |
	  | ColumnAdd | status                | String      |
	  | ColumnAdd | orderLocation         | Json        |
	  | ColumnAdd | orderContentTypeId    | Int         |
	  | ColumnAdd | orderDetails          | Json        |
	  | ColumnAdd | totalPrice            | Int         |
	  | ColumnAdd | partner_firstName     | String      |
	  | ColumnAdd | partner_lastName      | String      |
	  | ColumnAdd | partner_email         | String      |
	  | ColumnAdd | partner_latitude      | Double      |
	  | ColumnAdd | partner_longitude     | Double      |
	  | ColumnAdd | partner_imageUrl      | String      |
	  | ColumnAdd | partner_online        | Bool        |
	  | ColumnAdd | partner_userStatus    | String      |
	  | ColumnAdd | partner_statusMessage | String      |
	  | ColumnAdd | partner_ratingAvg     | Double      |
	  | ColumnAdd | rank                  | Int         |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action | partner_email | orderId                                              | orderDetails             | orderContentTypeId | orderLocation                    | status |
	  | RowAdd  |               | {client1_deliveryOrderController_createOrder_result} | ref://deliveryOrder.json | 1                  | ref://deliveryOrderLocation.json | PLACED |
	Given "client1" subscribed to report "orderRequest" with parameters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 1       |          |
	  | dimension_customerUserId | String  | @userId | exclude  |
	  | showOutOfRange           | String  | true    |          |
	  | partnerLatitude          | Integer | 0       |          |
	  | showUnrelated            | String  | true    |          |
	  | maxDistance              | Integer | 0       |          |
	  | partnerLongitude         | Integer | 0       |          |
	Then "client1" the following schema is received eventually on report "orderRequest"
	  | ~Action   | ~Name                 | ~ColumnType |
	  | ColumnAdd | orderId               | String      |
	  | ColumnAdd | status                | String      |
	  | ColumnAdd | orderLocation         | Json        |
	  | ColumnAdd | orderContentTypeId    | Int         |
	  | ColumnAdd | orderDetails          | Json        |
	  | ColumnAdd | totalPrice            | Int         |
	  | ColumnAdd | partner_firstName     | String      |
	  | ColumnAdd | partner_lastName      | String      |
	  | ColumnAdd | partner_email         | String      |
	  | ColumnAdd | partner_latitude      | Double      |
	  | ColumnAdd | partner_longitude     | Double      |
	  | ColumnAdd | partner_imageUrl      | String      |
	  | ColumnAdd | partner_online        | Bool        |
	  | ColumnAdd | partner_userStatus    | String      |
	  | ColumnAdd | partner_statusMessage | String      |
	  | ColumnAdd | partner_ratingAvg     | Double      |
	  | ColumnAdd | rank                  | Int         |
	Then "client1" the following data is received terminally on report "orderRequest"
	  | ~Action | partner_email | orderId |


  Scenario: Other users can see newly created job in their list
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value |
	  | dimension_contentTypeId | Integer | 1     |
	  | showOutOfRange          | String  | true  |
	  | partnerLatitude         | Integer | 0     |
	  | showUnrelated           | String  | true  |
	  | maxDistance             | Integer | 0     |
	  | partnerLongitude        | Integer | 0     |
	Then "client2" the following schema is received eventually on report "orderRequest"
	  | ~Action   | ~Name                 | ~ColumnType |
	  | ColumnAdd | orderId               | String      |
	  | ColumnAdd | status                | String      |
	  | ColumnAdd | orderLocation         | Json        |
	  | ColumnAdd | orderContentTypeId    | Int         |
	  | ColumnAdd | orderDetails          | Json        |
	  | ColumnAdd | totalPrice            | Int         |
	  | ColumnAdd | partner_firstName     | String      |
	  | ColumnAdd | partner_lastName      | String      |
	  | ColumnAdd | partner_email         | String      |
	  | ColumnAdd | partner_latitude      | Double      |
	  | ColumnAdd | partner_longitude     | Double      |
	  | ColumnAdd | partner_imageUrl      | String      |
	  | ColumnAdd | partner_online        | Bool        |
	  | ColumnAdd | partner_userStatus    | String      |
	  | ColumnAdd | partner_statusMessage | String      |
	  | ColumnAdd | partner_ratingAvg     | Double      |
	  | ColumnAdd | rank                  | Int         |
	Then "client2" the following data is received eventually on report "orderRequest"
	  | ~Action | orderId                                              | orderDetails             | orderLocation                    | status |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | ref://deliveryOrder.json | ref://deliveryOrderLocation.json | PLACED |


  Scenario: Responding to order causes order to appear in responded order list for partner
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                         | Type   | Value     |
	  | dimension_partnerId          | String | @userId   |
	  | dimension_partnerOrderStatus | String | RESPONDED |
	Then "client2" the following schema is received eventually on report "orderResponses"
	  | ~Action   | ~Name                 | ~ColumnType |
	  | ColumnAdd | orderId               | String      |
	  | ColumnAdd | partnerOrderStatus    | String      |
	  | ColumnAdd | orderLocation         | Json        |
	  | ColumnAdd | orderContentTypeId    | Int         |
	  | ColumnAdd | totalPrice            | Int         |
	  | ColumnAdd | partner_firstName     | String      |
	  | ColumnAdd | partner_lastName      | String      |
	  | ColumnAdd | partner_email         | String      |
	  | ColumnAdd | partner_latitude      | Double      |
	  | ColumnAdd | partner_longitude     | Double      |
	  | ColumnAdd | partner_imageUrl      | String      |
	  | ColumnAdd | partner_online        | Bool        |
	  | ColumnAdd | partner_userStatus    | String      |
	  | ColumnAdd | partner_statusMessage | String      |
	  | ColumnAdd | partner_ratingAvg     | Double      |
	  | ColumnAdd | orderDetails          | Json        |
	  | ColumnAdd | rank                  | Int         |
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                              | orderDetails             | partner_firstName | partner_lastName | orderLocation                    | partnerOrderStatus |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | ref://deliveryOrder.json | Modestas          | BrickLayer       | ref://deliveryOrderLocation.json | RESPONDED          |


  Scenario: Responding to order causes order to dissapear from order request list for partner who responded but still remain in order request list for the rest
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	When "client2" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value |
	  | dimension_contentTypeId | Integer | 1     |
	  | showOutOfRange          | String  | true  |
	  | partnerLatitude         | Integer | 0     |
	  | showUnrelated           | String  | true  |
	  | maxDistance             | Integer | 0     |
	  | partnerLongitude        | Integer | 0     |
	Then "client2" the following data is received terminally on report "orderRequest"
	  | ~Action | orderId | orderDetails | orderLocation | status |
	When "client3" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value |
	  | dimension_contentTypeId | Integer | 1     |
	  | showOutOfRange          | String  | true  |
	  | partnerLatitude         | Integer | 0     |
	  | showUnrelated           | String  | true  |
	  | maxDistance             | Integer | 0     |
	  | partnerLongitude        | Integer | 0     |
	Then "client3" the following data is received eventually on report "orderRequest"
	  | ~Action | orderId                                              | orderDetails             | orderLocation                    | status |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | ref://deliveryOrder.json | ref://deliveryOrderLocation.json | PLACED |


  Scenario: Accepting response causes order status to change to accepted for partner then declined for the remaining partners
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	Given "client3" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+2}"                                       |
	Given "client1" controller "deliveryOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                                |
	  | orderId   | {client1_deliveryOrderController_createOrder_result} |
	  | partnerId | {client3_partnerController_registerPartner_result}   |
	Given "client3" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client3" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                              | partnerOrderStatus |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | ACCEPTED           |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                              | partnerOrderStatus |
	  | RowAdd  | {client1_deliveryOrderController_createOrder_result} | DECLINED           |
