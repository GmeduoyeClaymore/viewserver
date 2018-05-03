Feature: Personell order scenarios

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


  Scenario: User can create personell order and see it in their posted order list but not in their request list
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    When "client1" subscribed to report "customerOrderSummary" with parameters
      | Name                     | Type   | Value           |
      | dimension_status         | String | ACCEPTED,PLACED |
      | dimension_customerUserId | String | @userId         |
    Then "client1" the following data is received eventually on report "customerOrderSummary"
      | ~Action | partner_email | orderId                                               | orderDetails                          | orderContentTypeId | orderLocation                                 | status |
      | RowAdd  |               | {client1_personellOrderController_createOrder_result} | ref://json/orders/personellOrder.json | 5                  | ref://json/orders/personellOrderLocation.json | PLACED |
    Given "client1" subscribed to report "orderRequest" with parameters
      | Name                     | Type    | Value   | Excluded |
      | dimension_contentTypeId  | Integer | 5       |          |
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
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" subscribed to report "orderRequest" with parameters
      | Name                    | Type    | Value  |
      | dimension_contentTypeId | Integer | 5      |
      | dimension_status        | String  | PLACED |
      | showOutOfRange          | String  | true   |
      | partnerLatitude         | Integer | 0      |
      | showUnrelated           | String  | true   |
      | maxDistance             | Integer | 0      |
      | partnerLongitude        | Integer | 0      |
    Then "client2" the following data is received eventually on report "orderRequest"
      | ~Action | orderId                                               | orderDetails                          | orderLocation                                 | status |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | ref://json/orders/personellOrder.json | ref://json/orders/personellOrderLocation.json | PLACED |


  Scenario: Responding to order causes order to appear in responded order list for partner
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
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
      | ColumnAdd | requiredDate          | DateTime    |
      | ColumnAdd | rank                  | Int         |
      | ColumnAdd | customer_firstName    | String      |
      | ColumnAdd | customer_lastName     | String      |
      | ColumnAdd | customer_ratingAvg    | Double      |
    Then "client2" the following data is received eventually on report "orderResponses"
      | ~Action | orderId                                               | orderDetails                          | partner_firstName | partner_lastName | orderLocation                                 | partnerOrderStatus |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | ref://json/orders/personellOrder.json | Modestas          | BrickLayer       | ref://json/orders/personellOrderLocation.json | RESPONDED          |


  Scenario: Cancelling Response to order removes order from the responses list
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client2" controller "personellOrderController" action "cancelResponsePartner" invoked with parameters
      | Name    | Value                                                 |
      | orderId | {client1_personellOrderController_createOrder_result} |
    Given "client2" subscribed to report "orderResponses" with parameters
      | Name                         | Type   | Value     |
      | dimension_partnerId          | String | @userId   |
      | dimension_partnerOrderStatus | String | RESPONDED |
    Then "client2" the following data is received terminally on report "orderResponses"
      | ~Action | orderId | orderDetails |


  Scenario: Responding to order causes order to dissapear from order request list for partner who responded but still remain in order request list for the rest
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    When "client2" subscribed to report "orderRequest" with parameters
      | Name                    | Type    | Value  |
      | dimension_contentTypeId | Integer | 5      |
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
      | dimension_contentTypeId | Integer | 5     |
      | showOutOfRange          | String  | true  |
      | partnerLatitude         | Integer | 0     |
      | showUnrelated           | String  | true  |
      | maxDistance             | Integer | 0     |
      | partnerLongitude        | Integer | 0     |
    Then "client3" the following data is received eventually on report "orderRequest"
      | ~Action | orderId                                               | orderDetails                          | orderLocation                                 | status |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | ref://json/orders/personellOrder.json | ref://json/orders/personellOrderLocation.json | PLACED |


  Scenario: Accepting response causes order status to change to accepted for partner then declined for the remaining partners
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client3" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+2}"                                        |
      | price         | 100                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client3_partnerController_registerPartner_result}    |
    Given "client3" subscribed to report "orderResponses" with parameters
      | Name                | Type   | Value   |
      | dimension_partnerId | String | @userId |
    Then "client3" the following data is received eventually on report "orderResponses"
      | ~Action | orderId                                               | partnerOrderStatus |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | ACCEPTED           |
    Given "client2" subscribed to report "orderResponses" with parameters
      | Name                | Type   | Value   |
      | dimension_partnerId | String | @userId |
    Then "client2" the following data is received eventually on report "orderResponses"
      | ~Action | orderId                                               | partnerOrderStatus |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | DECLINED           |

  Scenario: Cancelling accepted response causes job to go back into responded state
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client3" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+2}"                                        |
      | price         | 100                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client3_partnerController_registerPartner_result}    |
    Given "client3" controller "personellOrderController" action "cancelResponsePartner" invoked with parameters
      | Name    | Value                                                 |
      | orderId | {client1_personellOrderController_createOrder_result} |
    Given "client3" subscribed to report "orderResponses" with parameters
      | Name                | Type   | Value   |
      | dimension_partnerId | String | @userId |
    Then "client3" the following data is received eventually on report "orderResponses"
      | ~Action | orderId                                               | partnerOrderStatus |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | CANCELLED          |
    Given "client2" subscribed to report "orderResponses" with parameters
      | Name                | Type   | Value   |
      | dimension_partnerId | String | @userId |
    Then "client2" the following data is received eventually on report "orderResponses"
      | ~Action | orderId                                               | partnerOrderStatus |
      | RowAdd  | {client1_personellOrderController_createOrder_result} | RESPONDED          |

  Scenario: Accepting response removes job from order request list for all
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client3" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+2}"                                        |
      | price         | 100                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client3_partnerController_registerPartner_result}    |
    Given "client2" subscribed to report "orderRequest" with parameters
      | Name                    | Type    | Value  |
      | dimension_contentTypeId | Integer | 5      |
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
      | dimension_contentTypeId | Integer | 5      |
      | dimension_status        | string  | PLACED |
      | showOutOfRange          | String  | true   |
      | partnerLatitude         | Integer | 0      |
      | showUnrelated           | String  | true   |
      | maxDistance             | Integer | 0      |
      | partnerLongitude        | Integer | 0      |
    Then "client4" the following data is received terminally on report "orderRequest"
      | ~Action | orderId | orderDetails | orderLocation | status |


  Scenario: Partner completing job marking job marks job as partner complete and payment made
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client2_partnerController_registerPartner_result}    |
    Given "client1" controller "personellOrderController" action "customerCompleteAndPay" invoked with parameters
      | Name    | Value                                                 |
      | orderId | {client1_personellOrderController_createOrder_result} |
    When "client1" subscribed to report "customerOrderSummary" with parameters
      | Name                     | Type   | Value   |
      | dimension_customerUserId | String | @userId |
    Then "client1" the following data is received eventually on report "customerOrderSummary"
      | ~Action | partner_email | orderId                                               | status    |
      | RowAdd  |               | {client1_personellOrderController_createOrder_result} | COMPLETED |
    When "client1" subscribed to report "paymentsReport" with parameters
      | Name                     | Type   | Value   |
      | dimension_paidFromUserId | String | @userId |
    Given keyColumn is "paymentId"
    Then "client1" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidToUserId                                       | paymentId                                                        | totalPrice |
      | RowAdd  |               | {client2_partnerController_registerPartner_result} | {client1_personellOrderController_customerCompleteAndPay_result} | 105        |
    When "client2" subscribed to report "paymentsReport" with parameters
      | Name                   | Type   | Value   |
      | dimension_paidToUserId | String | @userId |
    Then "client2" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidFromUserId                                     | paymentId                                                        | totalPrice |
      | RowAdd  |               | {client1_partnerController_registerPartner_result} | {client1_personellOrderController_customerCompleteAndPay_result} | 105        |


  Scenario: Adding payment stage paying and completing
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client2_partnerController_registerPartner_result}    |
    Given "client2" controller "personellOrderController" action "addPaymentStage" invoked with parameters
      | Name             | Value                                                 |
      | orderId          | {client1_personellOrderController_createOrder_result} |
      | amount       | 10                                                    |
      | name             | "First fix electrical"                                |
      | description      | "All first fix electrical work"                       |
      | paymentStageType | Percentage                                            |
    Given "client2" controller "personellOrderController" action "startPaymentStage" invoked with parameters
      | Name           | Value                                                     |
      | orderId        | {client1_personellOrderController_createOrder_result}     |
      | paymentStageId | {client2_personellOrderController_addPaymentStage_result} |
    Given "client2" controller "personellOrderController" action "completePaymentStage" invoked with parameters
      | Name           | Value                                                     |
      | orderId        | {client1_personellOrderController_createOrder_result}     |
      | paymentStageId | {client2_personellOrderController_addPaymentStage_result} |
    Given "client1" controller "personellOrderController" action "payForPaymentStage" invoked with parameters
      | Name           | Value                                                     |
      | orderId        | {client1_personellOrderController_createOrder_result}     |
      | paymentStageId | {client2_personellOrderController_addPaymentStage_result} |
    When "client2" subscribed to report "paymentsReport" with parameters
      | Name                   | Type   | Value   |
      | dimension_paidToUserId | String | @userId |
    Given keyColumn is "paymentId"
    Then "client2" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidFromUserId                                     | paymentId                                                    | totalPrice |
      | RowAdd  |               | {client1_partnerController_registerPartner_result} | {client1_personellOrderController_payForPaymentStage_result} | 10         |

    When "client1" subscribed to report "paymentsReport" with parameters
      | Name                     | Type   | Value   |
      | dimension_paidFromUserId | String | @userId |
    Then "client1" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidToUserId                                       | paymentId                                                    | totalPrice |
      | RowAdd  |               | {client2_partnerController_registerPartner_result} | {client1_personellOrderController_payForPaymentStage_result} | 10         |


  Scenario: Logging a days work paying and completing
    Given "client1" controller "personellOrderController" action "createOrder" invoked with data file "json/orders/createPersonellOrder.json" with parameters
      | Name             | Value                                              |
      | param_customerId | {client1_partnerController_registerPartner_result} |
    Given "client2" controller "personellOrderController" action "respondToOrder" invoked with parameters
      | Name          | Value                                                 |
      | orderId       | {client1_personellOrderController_createOrder_result} |
      | estimatedDate | "{now_date+1}"                                        |
      | price         | 105                                                   |
    Given "client1" controller "personellOrderController" action "acceptResponse" invoked with parameters
      | Name      | Value                                                 |
      | orderId   | {client1_personellOrderController_createOrder_result} |
      | partnerId | {client2_partnerController_registerPartner_result}    |
    Given "client2" controller "personellOrderController" action "logDayStarted" invoked with parameters
      | Name    | Value                                                 |
      | orderId | {client1_personellOrderController_createOrder_result} |
    Given "client2" controller "personellOrderController" action "logDayComplete" invoked with parameters
      | Name           | Value                                                   |
      | orderId        | {client1_personellOrderController_createOrder_result}   |
      | paymentStageId | {client2_personellOrderController_logDayStarted_result} |
    Given "client2" controller "personellOrderController" action "completePaymentStage" invoked with parameters
      | Name           | Value                                                   |
      | orderId        | {client1_personellOrderController_createOrder_result}   |
      | paymentStageId | {client2_personellOrderController_logDayStarted_result} |
    Given "client1" controller "personellOrderController" action "payForPaymentStage" invoked with parameters
      | Name           | Value                                                   |
      | orderId        | {client1_personellOrderController_createOrder_result}   |
      | paymentStageId | {client2_personellOrderController_logDayStarted_result} |
    When "client2" subscribed to report "paymentsReport" with parameters
      | Name                   | Type   | Value   |
      | dimension_paidToUserId | String | @userId |
    Given keyColumn is "paymentId"
    Then "client2" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidFromUserId                                     | paymentId                                                    | totalPrice |
      | RowAdd  |               | {client1_partnerController_registerPartner_result} | {client1_personellOrderController_payForPaymentStage_result} | 105        |

    When "client1" subscribed to report "paymentsReport" with parameters
      | Name                     | Type   | Value   |
      | dimension_paidFromUserId | String | @userId |
    Then "client1" the following data is received eventually on report "paymentsReport"
      | ~Action | partner_email | paidToUserId                                       | paymentId                                                    | totalPrice |
      | RowAdd  |               | {client2_partnerController_registerPartner_result} | {client1_personellOrderController_payForPaymentStage_result} | 105        |

