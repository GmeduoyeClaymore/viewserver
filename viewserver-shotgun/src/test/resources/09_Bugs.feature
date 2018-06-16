Feature: Delivery order scenarios

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


  Scenario: Accepting response doesnt duplicate response JSON
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "json/orders/createDeliveryOrder.json" with parameters
	  | Name              | Value                                              |
	  | param_paymentCard | {client1_partnerController_registerPartner_result} |
	Given "client3" controller "deliveryOrderController" action "respondToOrder" invoked with parameters
	  | Name         | Value                                                |
	  | orderId      | {client1_deliveryOrderController_createOrder_result} |
	  | requiredDate | "{now_date+2}"                                       |
	Given "client1" controller "deliveryOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                                |
	  | orderId   | {client1_deliveryOrderController_createOrder_result} |
	  | partnerId | {client3_partnerController_registerPartner_result}   |
	When "client1" subscribed to report "customerOrderSummary" with parameters
	  | Name              | Type   | Value                                                |
	  | dimension_orderId | String | {client1_deliveryOrderController_createOrder_result} |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action   | orderId                                              | partnerResponses                                                                                                                                                    |
	  | RowAdd | {client1_deliveryOrderController_createOrder_result} | { "{client3_partnerController_registerPartner_result}" : {"spreadPartnerId":"{client3_partnerController_registerPartner_result}", "partner_userStatus" : "ONLINE"}} |
