Feature: Journey order scenarios

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


  Scenario: Starting job marks order as in progress
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "negotiatedOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	Given "client1" controller "negotiatedOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                                |
	  | orderId   | {client1_deliveryOrderController_createOrder_result} |
	  | partnerId | {client2_partnerController_registerPartner_result}   |
	Given "client2" controller "journeyBaseOrderController" action "startJourney" invoked with parameters
	  | Name    | Value                                                |
	  | orderId | {client1_deliveryOrderController_createOrder_result} |
	When "client1" subscribed to report "customerOrderSummary" with parameters
	  | Name                     | Type   | Value   |
	  | dimension_customerUserId | String | @userId |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action | partner_email | orderId                                              | status     |
	  | RowAdd  |               | {client1_deliveryOrderController_createOrder_result} | INPROGRESS |


  Scenario: Partner completing job marking job marks job as partner complete and payment made
	Given "client1" controller "deliveryOrderController" action "createOrder" invoked with data file "createDeliveryOrder.json" with parameters
	  | Name             | Value                                              |
	  | param_customerId | {client1_partnerController_registerPartner_result} |
	Given "client2" controller "negotiatedOrderController" action "respondToOrder" invoked with parameters
	  | Name          | Value                                                |
	  | orderId       | {client1_deliveryOrderController_createOrder_result} |
	  | estimatedDate | "{now_date+1}"                                       |
	Given "client1" controller "negotiatedOrderController" action "acceptResponse" invoked with parameters
	  | Name      | Value                                                |
	  | orderId   | {client1_deliveryOrderController_createOrder_result} |
	  | partnerId | {client2_partnerController_registerPartner_result}   |
	Given "client2" controller "journeyBaseOrderController" action "startJourney" invoked with parameters
	  | Name    | Value                                                |
	  | orderId | {client1_deliveryOrderController_createOrder_result} |
	Given "client2" controller "journeyBaseOrderController" action "completeJourney" invoked with parameters
	  | Name    | Value                                                |
	  | orderId | {client1_deliveryOrderController_createOrder_result} |
	Given "client1" controller "orderPaymentController" action "customerCompleteAndPay" invoked with parameters
	  | Name    | Value                                                |
	  | orderId | {client1_deliveryOrderController_createOrder_result} |
	When "client1" subscribed to report "customerOrderSummary" with parameters
	  | Name                     | Type   | Value   |
	  | dimension_customerUserId | String | @userId |
	Then "client1" the following data is received eventually on report "customerOrderSummary"
	  | ~Action | partner_email | orderId                                             | status    |
	  | RowAdd  |               | {client1_deliveryOrderController_createOrder_result} | COMPLETED |
	When "client1" subscribed to report "paymentsReport" with parameters
	  | Name                     | Type   | Value   |
	  | dimension_paidFromUserId | String | @userId |
	Given keyColumn is "paymentId"
	Then "client1" the following data is received eventually on report "paymentsReport"
	  | ~Action | partner_email | paidToUserId                                       | paymentId                                                      |
	  | RowAdd  |               | {client2_partnerController_registerPartner_result} | {client1_orderPaymentController_customerCompleteAndPay_result} |
	When "client2" subscribed to report "paymentsReport" with parameters
	  | Name                   | Type   | Value   |
	  | dimension_paidToUserId | String | @userId |
	Then "client2" the following data is received eventually on report "paymentsReport"
	  | ~Action | partner_email | paidFromUserId                                     | paymentId                                                      |
	  | RowAdd  |               | {client1_partnerController_registerPartner_result} | {client1_orderPaymentController_customerCompleteAndPay_result} |