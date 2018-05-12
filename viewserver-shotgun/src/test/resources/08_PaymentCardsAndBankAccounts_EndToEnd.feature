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


  Scenario: Can add payment card for user
	Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                    |
	  | paymentCard | ref://json/payments/visaPaymentCard.json |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | paymentCards                                                                                    | userId                                             |
	  | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}} | {client1_partnerController_registerPartner_result} |

  Scenario: Can add multiple payment cards for user
	Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                    |
	  | paymentCard | ref://json/payments/visaPaymentCard.json |
	And "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                          |
	  | paymentCard | ref://json/payments/masterCardPaymentCard.json |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | paymentCards                                                                                                                                                                                       | userId                                             |
	  | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}, "1" : {"brand": "MasterCard", "last4": "4444", "expMonth": 8, "expYear": 2022, "isDefault": true}} | {client1_partnerController_registerPartner_result} |

  Scenario: Can delete payment card for user
	Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                    |
	  | paymentCard | ref://json/payments/visaPaymentCard.json |
	And "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                          |
	  | paymentCard | ref://json/payments/masterCardPaymentCard.json |
	And "client1" controller "userController" action "deletePaymentCard" invoked with parameters
	  | Name   | Value  |
	  | cardId | "1234" |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |

  Scenario: Can set default payment card for user
	Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                    |
	  | paymentCard | ref://json/payments/visaPaymentCard.json |
	And "client1" controller "userController" action "addPaymentCard" invoked with parameters
	  | Name        | Value                                          |
	  | paymentCard | ref://json/payments/masterCardPaymentCard.json |
	And "client1" controller "userController" action "setDefaultPaymentCard" invoked with parameters
	  | Name   | Value  |
	  | cardId | "12345 |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | paymentCards                                                                                                                                                                                       | userId                                             |
	  | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}, "1" : {"brand": "MasterCard", "last4": "4444", "expMonth": 8, "expYear": 2022, "isDefault": true}} | {client1_partnerController_registerPartner_result} |


  Scenario: Can add bank account for user
	Given "client1" controller "userController" action "setBankAccount" invoked with parameters
	  | Name               | Value                                                        |
	  | paymentBankAccount | ref://json/payments/bankAccount1.json                        |
	  | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | bankAccount                                                                                | userId                                             |
	  | RowAdd  | {"last4": "2345", "sortCode": "10-88-00", "bankName": "STRIPE TEST BANK", "country": "GB"} | {client1_partnerController_registerPartner_result} |


  Scenario: Can update bank account for user
	Given "client1" controller "userController" action "setBankAccount" invoked with parameters
	  | Name               | Value                                                        |
	  | paymentBankAccount | ref://json/payments/bankAccount1.json                        |
	  | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
	Given "client1" controller "userController" action "setBankAccount" invoked with parameters
	  | Name               | Value                                                        |
	  | paymentBankAccount | ref://json/payments/bankAccount2.json                        |
	  | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
	When "client1" subscribed to report "userReport" with parameters
	  | Name             | Type   | Value   |
	  | dimension_userId | String | @userId |
	Then "client1" the following data is received eventually on report "userReport"
	  | ~Action | bankAccount                                                                                | userId                                             |
	  | RowAdd  | {"last4": "1116", "sortCode": "10-88-00", "bankName": "STRIPE TEST BANK", "country": "GB"} | {client1_partnerController_registerPartner_result} |





