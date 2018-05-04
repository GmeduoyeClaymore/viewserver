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
    Then "client2" the following data is received eventually on report "userReport"
      | ~Action | contactNo   | distance | email                        | firstName | imageUrl | initiatedByMe | lastName   | latitude | longitude | market | notional | online | range | rank | ratingAvg | relationshipStatus | selectedContentTypes                        | statusMessage | type    | userId                                             |
      | RowAdd  | 07966265016 |          | modestasbricklayer@gmail.com | Modestas  |          |               | BrickLayer | 0.0      | 0.0       |        |          | True   | 50    | 0    | -1.0      |                    | {"5":{"selectedProductIds":["BrickLayer"]}} |               | partner | {client2_partnerController_registerPartner_result} |

  Scenario: Newly registered users can be seen by each otherß

  Scenario: Can see partner for product in userProduct report
    Given "client1" report parameters
      | Name           | Type    | Value |
      | showOutOfRange | String  | false |
      | showUnrelated  | String  | true  |
      | latitude       | Integer | 0     |
      | longitude      | Integer | 0     |
      | userId         | Integer | 0     |
      | maxDistance    | Integer | 0     |
    Given "client1" dimension filters
      | Name                | Type   | Value      |
      | dimension_productId | String | BrickLayer |
    And "client1" paging from 0 to 100 by "userId" descending
    When "client1" subscribed to report "usersForProductAll"
    Then "client1" the following schema is received eventually on report "usersForProductAll"
      | ~Action   | ~ColumnType | ~Name                |
      | ColumnAdd | String      | relationshipStatus   |
      | ColumnAdd | Bool        | initiatedByMe        |
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
      | ColumnAdd | Double      | ratingAvg            |
      | ColumnAdd | Double      | distance             |
      | ColumnAdd | Int         | rank                 |
    Then "client1" the following data is received eventually on report "usersForProductAll"
      | ~Action | contactNo   | distance | email                        | firstName | id | imageUrl | initiatedByMe | lastName   | latitude | longitude | market | notional | online | range | rank | ratingAvg | relationshipStatus | selectedContentTypes                        | statusMessage | type    | userId                                             |
      | RowAdd  | 07966265016 | 0.0      | modestasbricklayer@gmail.com | Modestas  |    |          | Null          | BrickLayer | 0.0      | 0.0       |        |          | true   | 50    | 0    | -1.0      |                    | {"5":{"selectedProductIds":["BrickLayer"]}} |               | partner | {client2_partnerController_registerPartner_result} |


  Scenario: Can add rating for users
    Given "client2" controller "userController" action "addOrUpdateRating" invoked with parameters
      | Name       | Value                                              |
      | orderId    | XXXXXXX                                            |
      | userId     | {client1_partnerController_registerPartner_result} |
      | rating     | 5                                                  |
      | comments   | "Foo comment"                                      |
      | ratingType | Customer                                           |
    When "client1" subscribed to report "userReport" with parameters
      | Name             | Type   | Value   |
      | dimension_userId | String | @userId |
    Then "client1" the following data is received eventually on report "userReport"
      | ~Action |  | ratings                                                                      | ratingAvg | userId                                             |
      | RowAdd  |  | { "0" : {"fromUserId":"{client2_partnerController_registerPartner_result}"}} | 5.0       | {client1_partnerController_registerPartner_result} |


  Scenario: Can add multiple ratings for users
    Given "client2" controller "userController" action "addOrUpdateRating" invoked with parameters
      | Name       | Value                                              |
      | orderId    | XXXXXXX                                            |
      | userId     | {client1_partnerController_registerPartner_result} |
      | rating     | 5                                                  |
      | comments   | "Foo comment"                                      |
      | ratingType | Customer                                           |
    Given "client3" controller "userController" action "addOrUpdateRating" invoked with parameters
      | Name       | Value                                              |
      | orderId    | YYYYYYYY                                           |
      | userId     | {client1_partnerController_registerPartner_result} |
      | rating     | 2                                                  |
      | comments   | "Foo comment too"                                  |
      | ratingType | Customer                                           |
    When "client1" subscribed to report "userReport" with parameters
      | Name             | Type   | Value   |
      | dimension_userId | String | @userId |
    Then "client1" the following data is received eventually on report "userReport"
      | ~Action |  | ratings                                                                                                                                                   | ratingAvg | userId                                             |
      | RowAdd  |  | { "0" : {"fromUserId":"{client2_partnerController_registerPartner_result}"} ,  "1" : {"fromUserId":"{client3_partnerController_registerPartner_result}"}} | 3.5       | {client1_partnerController_registerPartner_result} |

 #Scenario: Can add payment card for user
 #  Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
 #    | Name        | Value                                    |
 #    | paymentCard | ref://json/payments/visaPaymentCard.json |
 #  When "client1" subscribed to report "userReport" with parameters
 #    | Name             | Type   | Value   |
 #    | dimension_userId | String | @userId |
 #  Then "client1" the following data is received eventually on report "userReport"
 #    | ~Action | paymentCards                                                                                    | userId                                             |
 #    | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}} | {client1_partnerController_registerPartner_result} |

 #Scenario: Can add multiple payment cards for user
 #  Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
 #    | Name        | Value                                    |
 #    | paymentCard | ref://json/payments/visaPaymentCard.json |
 #  And "client1" controller "userController" action "addPaymentCard" invoked with parameters
 #    | Name        | Value                                          |
 #    | paymentCard | ref://json/payments/masterCardPaymentCard.json |
 #  When "client1" subscribed to report "userReport" with parameters
 #    | Name             | Type   | Value   |
 #    | dimension_userId | String | @userId |
 #  Then "client1" the following data is received eventually on report "userReport"
 #    | ~Action | paymentCards                                                                                                                                                                                       | userId                                             |
 #    | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}, "1" : {"brand": "MasterCard", "last4": "4444", "expMonth": 8, "expYear": 2022, "isDefault": true}} | {client1_partnerController_registerPartner_result} |

#  Scenario: Can delete payment card for user
#    Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
#      | Name        | Value                                    |
#      | paymentCard | ref://json/payments/visaPaymentCard.json |
#    And "client1" controller "userController" action "addPaymentCard" invoked with parameters
#      | Name        | Value                                          |
#      | paymentCard | ref://json/payments/masterCardPaymentCard.json |
#    And "client1" controller "userController" action "deletePaymentCard" invoked with parameters
#      | Name   | Value  |
#      | cardId | "1234" |
#    When "client1" subscribed to report "userReport" with parameters
#      | Name             | Type   | Value   |
#      | dimension_userId | String | @userId |

#Scenario: Can set default payment card for user
#  Given "client1" controller "userController" action "addPaymentCard" invoked with parameters
#    | Name        | Value                                    |
#    | paymentCard | ref://json/payments/visaPaymentCard.json |
#  And "client1" controller "userController" action "addPaymentCard" invoked with parameters
#    | Name        | Value                                          |
#    | paymentCard | ref://json/payments/masterCardPaymentCard.json |
#  And "client1" controller "userController" action "setDefaultPaymentCard" invoked with parameters
#    | Name   | Value  |
#    | cardId | "12345 |
#  When "client1" subscribed to report "userReport" with parameters
#    | Name             | Type   | Value   |
#    | dimension_userId | String | @userId |
#  Then "client1" the following data is received eventually on report "userReport"
#    | ~Action | paymentCards                                                                                                                                                                                       | userId                                             |
#    | RowAdd  | { "0" : {"brand": "Visa", "last4": "4242", "expMonth": 11, "expYear": 2025, "isDefault": true}, "1" : {"brand": "MasterCard", "last4": "4444", "expMonth": 8, "expYear": 2022, "isDefault": true}} | {client1_partnerController_registerPartner_result} |


# Scenario: Can add bank account for user
#   Given "client1" controller "userController" action "setBankAccount" invoked with parameters
#     | Name               | Value                                                        |
#     | paymentBankAccount | ref://json/payments/bankAccount1.json                        |
#     | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
#   When "client1" subscribed to report "userReport" with parameters
#     | Name             | Type   | Value   |
#     | dimension_userId | String | @userId |
#   Then "client1" the following data is received eventually on report "userReport"
#     | ~Action | bankAccount                                                                                | userId                                             |
#     | RowAdd  | {"last4": "2345", "sortCode": "10-88-00", "bankName": "STRIPE TEST BANK", "country": "GB"} | {client1_partnerController_registerPartner_result} |


# Scenario: Can update bank account for user
#   Given "client1" controller "userController" action "setBankAccount" invoked with parameters
#     | Name               | Value                                                        |
#     | paymentBankAccount | ref://json/payments/bankAccount1.json                        |
#     | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
#   Given "client1" controller "userController" action "setBankAccount" invoked with parameters
#     | Name               | Value                                                        |
#     | paymentBankAccount | ref://json/payments/bankAccount2.json                        |
#     | address            | ref://json/deliveryAddress/deliveryAddress12KinnoulRoad.json |
#   When "client1" subscribed to report "userReport" with parameters
#     | Name             | Type   | Value   |
#     | dimension_userId | String | @userId |
#   Then "client1" the following data is received eventually on report "userReport"
#     | ~Action | bankAccount                                                                                | userId                                             |
#     | RowAdd  | {"last4": "1116", "sortCode": "10-88-00", "bankName": "STRIPE TEST BANK", "country": "GB"} | {client1_partnerController_registerPartner_result} |





