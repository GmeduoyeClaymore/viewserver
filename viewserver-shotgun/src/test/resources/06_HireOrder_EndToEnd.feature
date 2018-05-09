Feature: Hire order scenarios

  Background:
	Given a running shotgun viewserver
	Given a client named "client1" connected to "{client1.url}"
	Given a client named "client2" connected to "{client2.url}"
	Given a client named "client3" connected to "{client3.url}"
	Given a client named "client4" connected to "{client4.url}"
	Given keyColumn is "orderId"
	Given "client1" All data sources are built
	Given "client1" controller "partnerController" action "registerPartner" invoked with data file "json/users/allRounder.json"
	Given "client2" controller "partnerController" action "registerPartner" invoked with data file "json/users/brickLayerRegistration.json"
	Given "client3" controller "partnerController" action "registerPartner" invoked with data file "json/users/plastererRegistration.json"
	Given "client4" controller "partnerController" action "registerPartner" invoked with data file "json/users/groundWorkerRegistration.json"


  Scenario: User can create hire order and see it in their posted order list but not in their request list
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	When "client1" subscribed to report "customerOrderSummary" with parameters
	  | Name                     | Type   | Value           |
	  | dimension_status         | String | ACCEPTED,PLACED |
	  | dimension_customerUserId | String | @userId         |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action | partner_email | orderId                                          | orderDetails                     | orderContentTypeId | orderLocation                            | status |
	  | RowAdd  |               | {client1_hireOrderController_createOrder_result} | ref://json/orders/hireOrder.json | 3                  | ref://json/orders/hireOrderLocation.json | PLACED |
	Given "client1" subscribed to report "orderRequest" with parameters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 3       |          |
	  | dimension_customerUserId | String  | @userId | exclude  |
	  | dimension_status         | String  | PLACED  |          |
	  | showOutOfRange           | String  | true    |          |
	  | partnerLatitude          | Integer | 0       |          |
	  | showUnrelated            | String  | true    |          |
	  | maxDistance              | Integer | 0       |          |
	  | partnerLongitude         | Integer | 0       |          |
	Then "client1" the following data is received terminally on report "orderRequest"
	  | ~Action | partner_email | orderId |


  Scenario: Other users can see newly created job in their list
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value  |
	  | dimension_contentTypeId | Integer | 3      |
	  | dimension_status        | String  | PLACED |
	  | showOutOfRange          | String  | true   |
	  | partnerLatitude         | Integer | 0      |
	  | showUnrelated           | String  | true   |
	  | maxDistance             | Integer | 0      |
	  | partnerLongitude        | Integer | 0      |
	Then "client2" the following data is received eventually on report "orderRequest"
	  | ~Action | orderId                                          | orderDetails                     | orderLocation                            | status |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | ref://json/orders/hireOrder.json | ref://json/orders/hireOrderLocation.json | PLACED |


  Scenario: Responding to order causes order to appear in responded order list for partner
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                     | Type   | Value     |
	  | dimension_partnerId      | String | @userId   |
	  | dimension_responseStatus | String | RESPONDED |
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                          | orderDetails                     | partner_firstName | partner_lastName | orderLocation                            | responseStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | ref://json/orders/hireOrder.json | Modestas          | BrickLayer       | ref://json/orders/hireOrderLocation.json | RESPONDED      |


  Scenario: Cancelling Response to order removes order from the responses list
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client2" controller "hireOrderController" action "cancelResponsePartner" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                     | Type   | Value     |
	  | dimension_partnerId      | String | @userId   |
	  | dimension_responseStatus | String | RESPONDED |
	Then "client2" the following data is received terminally on report "orderResponses"
	  | ~Action | orderId | orderDetails |


  Scenario: Responding to order causes order to dissapear from order request list for partner who responded but still remain in order request list for the rest
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	When "client2" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value  |
	  | dimension_contentTypeId | Integer | 3      |
	  | dimension_status        | String  | PLACED |
	  | showOutOfRange          | String  | true   |
	  | partnerLatitude         | Integer | 0      |
	  | showUnrelated           | String  | true   |
	  | maxDistance             | Integer | 0      |
	  | partnerLongitude        | Integer | 0      |
	Then "client2" the following data is received terminally on report "orderRequest"
	  | ~Action | orderId | orderDetails | orderLocation | status |
	When "client3" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value |
	  | dimension_contentTypeId | Integer | 3     |
	  | showOutOfRange          | String  | true  |
	  | partnerLatitude         | Integer | 0     |
	  | showUnrelated           | String  | true  |
	  | maxDistance             | Integer | 0     |
	  | partnerLongitude        | Integer | 0     |
	Then "client3" the following data is received eventually on report "orderRequest"
	  | ~Action | orderId                                          | orderDetails                     | orderLocation                            | status |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | ref://json/orders/hireOrder.json | ref://json/orders/hireOrderLocation.json | PLACED |


  Scenario: Accepting response causes order status to change to accepted for partner then declined for the remaining partners
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client3" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+2}"                                   |
	  | amount       | 100                                              |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                              |
	  | orderId   | {client1_hireOrderController_createOrder_result}   |
	  | partnerId | {client3_partnerController_registerPartner_result} |
	Given "client3" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client3" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                          | responseStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | ACCEPTED       |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                          | responseStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | REJECTED       |

  Scenario: Cancelling accepted response causes job to go back into responded state
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client3" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+2}"                                   |
	  | amount       | 100                                              |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                              |
	  | orderId   | {client1_hireOrderController_createOrder_result}   |
	  | partnerId | {client3_partnerController_registerPartner_result} |
	Given "client3" controller "hireOrderController" action "cancelResponsePartner" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	Given "client3" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client3" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                          | responseStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | CANCELLED      |
	Given "client2" subscribed to report "orderResponses" with parameters
	  | Name                | Type   | Value   |
	  | dimension_partnerId | String | @userId |
	Then "client2" the following data is received eventually on report "orderResponses"
	  | ~Action | orderId                                          | responseStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | RESPONDED      |

  Scenario: Accepting response removes job from order request list for all
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client3" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+2}"                                   |
	  | amount       | 100                                              |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                              |
	  | orderId   | {client1_hireOrderController_createOrder_result}   |
	  | partnerId | {client3_partnerController_registerPartner_result} |
	Given "client2" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value  |
	  | dimension_contentTypeId | Integer | 3      |
	  | dimension_status        | string  | PLACED |
	  | showOutOfRange          | String  | true   |
	  | partnerLatitude         | Integer | 0      |
	  | showUnrelated           | String  | true   |
	  | maxDistance             | Integer | 0      |
	  | partnerLongitude        | Integer | 0      |
	Then "client2" the following data is received terminally on report "orderRequest"
	  | ~Action | orderId | orderDetails | orderLocation | status |
	Given "client4" subscribed to report "orderRequest" with parameters
	  | Name                    | Type    | Value  |
	  | dimension_contentTypeId | Integer | 3      |
	  | dimension_status        | string  | PLACED |
	  | showOutOfRange          | String  | true   |
	  | partnerLatitude         | Integer | 0      |
	  | showUnrelated           | String  | true   |
	  | maxDistance             | Integer | 0      |
	  | partnerLongitude        | Integer | 0      |
	Then "client4" the following data is received terminally on report "orderRequest"
	  | ~Action | orderId | orderDetails | orderLocation | status |


  Scenario: Can create delivery for hire item marking the job as item ready
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client3" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+2}"                                   |
	  | amount       | 100                                              |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                              |
	  | orderId   | {client1_hireOrderController_createOrder_result}   |
	  | partnerId | {client3_partnerController_registerPartner_result} |
	Given "client1" controller "hireOrderController" action "markItemReady" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	Given "client1" controller "hireOrderController" action "generateOutboundDeliveryOrder" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	Given "client1" controller "hireOrderController" action "createDeliveryOrder" invoked with parameters
	  | Name            | Value                                                              |
	  | order           | {client1_hireOrderController_generateOutboundDeliveryOrder_result} |
	  | paymentMethodId | XXXX                                                               |
	Given "client4" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                                    |
	  | orderId      | {client1_hireOrderController_createDeliveryOrder_result} |
	  | requiredDate | "{now_date+1}"                                           |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	Given "client4" controller "hireOrderController" action "startJourney" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	When "client1" subscribed to report "customerOrderSummary" with "dimension_orderId" is "{client1_hireOrderController_createOrder_result}"
	  | ~Action | orderId                                          | orderDetails.hireOrderStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | OUTFORDELIVERY               |
	Given "client4" controller "hireOrderController" action "completeJourney" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	Then "client1" the following data is received terminally on report "customerOrderSummary"
	  | ~Action | orderId                                          | orderDetails.hireOrderStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | ONHIRE                       |
	Given "client1" controller "hireOrderController" action "generateInboundDeliveryOrder" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	Given "client1" controller "hireOrderController" action "createDeliveryOrder" invoked with parameters
	  | Name            | Value                                                             |
	  | order           | {client1_hireOrderController_generateInboundDeliveryOrder_result} |
	  | paymentMethodId | XXXX                                                              |
	Given "client4" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                                    |
	  | orderId      | {client1_hireOrderController_createDeliveryOrder_result} |
	  | requiredDate | "{now_date+1}"                                           |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	Given "client4" controller "hireOrderController" action "startJourney" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	Then "client1" subscribed to report "customerOrderSummary" with "dimension_orderId" is "{client1_hireOrderController_createOrder_result}"
	  | ~Action | orderId                                          | orderDetails.hireOrderStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | OFFHIRE                      |
	Given "client4" controller "hireOrderController" action "completeJourney" invoked with parameters
	  | Name    | Value                                                    |
	  | orderId | {client1_hireOrderController_createDeliveryOrder_result} |
	When "client1" subscribed to report "customerOrderSummary" with "dimension_orderId" is "{client1_hireOrderController_createOrder_result}"
	  | ~Action | orderId                                          | orderDetails.hireOrderStatus |
	  | RowAdd  | {client1_hireOrderController_createOrder_result} | COMPLETE                     |


  Scenario: Partner completing job marking job marks job as partner complete and payment made
	Given "client1" controller "hireOrderController" action "createOrder" invoked with data file "json/orders/createHireOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "hireOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                            |
	  | orderId      | {client1_hireOrderController_createOrder_result} |
	  | requiredDate | "{now_date+1}"                                   |
	  | amount       | 105                                              |
	Given "client1" controller "hireOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                              |
	  | orderId   | {client1_hireOrderController_createOrder_result}   |
	  | partnerId | {client2_partnerController_registerPartner_result} |
	Given "client1" controller "hireOrderController" action "customerCompleteAndPay" invoked with parameters
	  | Name    | Value                                            |
	  | orderId | {client1_hireOrderController_createOrder_result} |
	When "client1" subscribed to report "customerOrderSummary" with parameters
	  | Name                     | Type   | Value   |
	  | dimension_customerUserId | String | @userId |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action | partner_email                | orderId                                          | status    |
	  | RowAdd  | modestasbricklayer@gmail.com | {client1_hireOrderController_createOrder_result} | COMPLETED |
	When "client1" subscribed to report "paymentsReport" with parameters
	  | Name                     | Type   | Value   |
	  | dimension_paidFromUserId | String | @userId |
	Given keyColumn is "paymentId"
	Then "client1" the following data is received eventually on report "paymentsReport"
	  | ~Action | partner_email | paidToUserId                                       | paymentId                                                   | totalPrice |
	  | RowAdd  |               | {client2_partnerController_registerPartner_result} | {client1_hireOrderController_customerCompleteAndPay_result} | 105        |
	When "client2" subscribed to report "paymentsReport" with parameters
	  | Name                   | Type   | Value   |
	  | dimension_paidToUserId | String | @userId |
	Then "client2" the following data is received eventually on report "paymentsReport"
	  | ~Action | partner_email | paidFromUserId                                     | paymentId                                                   | totalPrice |
	  | RowAdd  |               | {client1_partnerController_registerPartner_result} | {client1_hireOrderController_customerCompleteAndPay_result} | 105        |


	  | Row| dimension_partnerId | spreadPartnerId | dimension_responseStatus | responseStatus | estimatedDate  | price  | orderDetails                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | dimension_orderId | orderId                               | dimension_status | status    | requiredDate | orderLocation                                                                                                                                                                                                                                                                                                                | dimension_customerUserId | userId | orderContentTypeId | totalPrice | customer_firstName | customer_lastName | customer_ratingAvg | dimension_productId | dimension_contentTypeId | partner_userId | partner_firstName | partner_lastName | partner_email            | partner_ratingAvg | partner_latitude | partner_longitude | partner_range | partner_imageUrl                                                                                                                                          | partner_online | partner_userStatus | partner_statusMessage |
	  +----+---------------------+-----------------+--------------------------+----------------+----------------+--------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------------------+---------------------------------------+------------------+-----------+--------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+--------------------------+--------+--------------------+------------+--------------------+-------------------+--------------------+---------------------+-------------------------+----------------+-------------------+------------------+--------------------------+-------------------+------------------+-------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+----------------+--------------------+-----------------------+
	  | 1  | 3                   | null            | 0                        | ACCEPTED       | 1525935600000  | 15000  | {"amountToPay":15000,"assignedPartner":{"date":"2018-05-10T08:00:00.000+01:00","price":15000,"partnerId":"3ABCD3","responseStatus":"ACCEPTED"},"amount":15000,"negotiatedOrderStatus":"ASSIGNED","productId":"BrickLayer","orderId":"4a9ec175-4a00-4972-94c5-956eaffb946f","origin":{"googlePlaceId":"ChIJ4ao3sU0bdkgR2fd7b4vQBwI","city":"London","created":"2017-01-01T08:30:00.000Z","latitude":51.5182655,"userId":"3ABCD","rowId":0,"deliveryAddressId":"ay3abef8-6611-449d-a91d-ea1fca7cfddd","isDefault":true,"rank":0,"postCode":"EC1N 8LY","line1":"110 Hatton Garden","key":0,"longitude":-0.1079763},"requiredDate":"2018-05-10T07:00:00.000Z","description":"Xxx","orderStatus":"ACCEPTED","title":"Brick Layer","paymentType":"DAYRATE","partnerUserId":"3ABCD3","customerUserId":"3ABCD","orderProduct":{"dimension_productId":9,"productId":"BrickLayer","price":15000,"name":"Brick Layer","description":"","rank":0,"categoryId":"1Workers","key":0,"rowId":0},"orderContentTypeId":5}  | 0                 | 4a9ec175-4a00-4972-94c5-956eaffb946f  | 0                | ACCEPTED  | 0            | {"googlePlaceId":"ChIJ4ao3sU0bdkgR2fd7b4vQBwI","city":"London","created":"2017-01-01T08:30:00.000Z","latitude":51.5182655,"userId":"3ABCD","rowId":0,"deliveryAddressId":"ay3abef8-6611-449d-a91d-ea1fca7cfddd","isDefault":true,"rank":0,"postCode":"EC1N 8LY","line1":"110 Hatton Garden","key":0,"longitude":-0.1079763}  | 0                        | 3ABCD  | 5                  | 0          | Bob                | Builder           | -1.0               | 9                   | 3                       | 3ABCD3         | Andrew            | Davies           | andrew.davies@email.com  | -1.0              | 51.50882498      | -0.06413978       | 5             | https://static1.squarespace.com/static/53a05d04e4b03f568ef6dc69/58bdf1c703596e79663bfab9/58bdf1f1b8a79b7c5994bcf7/1491030762778/MH_010317_Windows_27.JPG  | true           | ONLINE             | null                  |
	  +----+---------------------+-----------------+--------------------------+----------------+----------------+--------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------------------+---------------------------------------+------------------+-----------+--------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+--------------------------+--------+--------------------+------------+--------------------+-------------------+--------------------+---------------------+-------------------------+----------------+-------------------+------------------+--------------------------+-------------------+------------------+-------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+----------------+--------------------+-----------------------+

	  18/05/09 05:05:10.049 [reactor-main] WARN  io.viewserver.operators.group.GroupByOperator - +----+---------------------+-----------------+--------------------------+----------------+----------------+--------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------------------+---------------------------------------+------------------+-----------+--------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+--------------------------+--------+--------------------+------------+--------------------+-------------------+--------------------+---------------------+-------------------------+----------------+-------------------+------------------+--------------------------+-------------------+------------------+-------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+----------------+--------------------+-----------------------+
	  | Row| dimension_partnerId | spreadPartnerId | dimension_responseStatus | responseStatus | estimatedDate  | price  | orderDetails                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | dimension_orderId | orderId                               | dimension_status | status    | requiredDate | orderLocation                                                                                                                                                                                                                                                                                                                | dimension_customerUserId | userId | orderContentTypeId | totalPrice | customer_firstName | customer_lastName | customer_ratingAvg | dimension_productId | dimension_contentTypeId | partner_userId | partner_firstName | partner_lastName | partner_email            | partner_ratingAvg | partner_latitude | partner_longitude | partner_range | partner_imageUrl                                                                                                                                          | partner_online | partner_userStatus | partner_statusMessage |
	  +----+---------------------+-----------------+--------------------------+----------------+----------------+--------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------------------+---------------------------------------+------------------+-----------+--------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+--------------------------+--------+--------------------+------------+--------------------+-------------------+--------------------+---------------------+-------------------------+----------------+-------------------+------------------+--------------------------+-------------------+------------------+-------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+----------------+--------------------+-----------------------+
	  | 1  | 4                   | 3ABCD3          | 3                        | ACCEPTED       | 1525935600000  | 15000  | {"amountToPay":15000,"assignedPartner":{"date":"2018-05-10T08:00:00.000+01:00","price":15000,"partnerId":"3ABCD3","responseStatus":"ACCEPTED"},"amount":15000,"negotiatedOrderStatus":"ASSIGNED","productId":"BrickLayer","orderId":"4a9ec175-4a00-4972-94c5-956eaffb946f","origin":{"googlePlaceId":"ChIJ4ao3sU0bdkgR2fd7b4vQBwI","city":"London","created":"2017-01-01T08:30:00.000Z","latitude":51.5182655,"userId":"3ABCD","rowId":0,"deliveryAddressId":"ay3abef8-6611-449d-a91d-ea1fca7cfddd","isDefault":true,"rank":0,"postCode":"EC1N 8LY","line1":"110 Hatton Garden","key":0,"longitude":-0.1079763},"requiredDate":"2018-05-10T07:00:00.000Z","description":"Xxx","orderStatus":"ACCEPTED","title":"Brick Layer","paymentType":"DAYRATE","partnerUserId":"3ABCD3","customerUserId":"3ABCD","orderProduct":{"dimension_productId":9,"productId":"BrickLayer","price":15000,"name":"Brick Layer","description":"","rank":0,"categoryId":"1Workers","key":0,"rowId":0},"orderContentTypeId":5}  | 11                | 4a9ec175-4a00-4972-94c5-956eaffb946f  | 2                | ACCEPTED  | 0            | {"googlePlaceId":"ChIJ4ao3sU0bdkgR2fd7b4vQBwI","city":"London","created":"2017-01-01T08:30:00.000Z","latitude":51.5182655,"userId":"3ABCD","rowId":0,"deliveryAddressId":"ay3abef8-6611-449d-a91d-ea1fca7cfddd","isDefault":true,"rank":0,"postCode":"EC1N 8LY","line1":"110 Hatton Garden","key":0,"longitude":-0.1079763}  | 2                        | 3ABCD  | 5                  | 0          | Bob                | Builder           | -1.0               | 9                   | 3                       | 3ABCD3         | Andrew            | Davies           | andrew.davies@email.com  | -1.0              | 51.50882498      | -0.06413978       | 5             | https://static1.squarespace.com/static/53a05d04e4b03f568ef6dc69/58bdf1c703596e79663bfab9/58bdf1f1b8a79b7c5994bcf7/1491030762778/MH_010317_Windows_27.JPG  | true           | ONLINE             | null                  |
	  +----+---------------------+-----------------+--------------------------+----------------+----------------+--------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------------------+---------------------------------------+------------------+-----------+--------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+--------------------------+--------+--------------------+------------+--------------------+-------------------+--------------------+---------------------+-------------------------+----------------+-------------------+------------------+--------------------------+-------------------+------------------+-------------------+---------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+----------------+--------------------+-----------------------+
