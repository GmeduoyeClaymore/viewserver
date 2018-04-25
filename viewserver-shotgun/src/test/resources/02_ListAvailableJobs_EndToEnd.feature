Feature: List available job scenarios

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


  Scenario: User can see all delivery jobs
	Given "client1" dimension filters
	  | Name                     | Type    | Value   | Excluded |
	  | dimension_contentTypeId  | Integer | 1       |          |
	  | dimension_customerUserId | String  | @userId | exlcuded |
	Given "client1" report parameters
	  | Name             | Type    | Value |
	  | showOutOfRange   | String  | true  |
	  | partnerLatitude  | Integer | 0     |
	  | showUnrelated    | String  | true  |
	  | maxDistance      | Integer | 0     |
	  | partnerLongitude | Integer | 0     |
	When "client1" subscribed to report "orderRequest"
	Then "client1" the following schema is received eventually on report "orderRequest"
	  | ~Action   | ~Name                          | ~ColumnType |
	  | ColumnAdd | orderId                        | String      |
	  | ColumnAdd | status                         | String      |
	  | ColumnAdd | totalPrice                     | Int         |
	  | ColumnAdd | rank                           | Int         |
	  | ColumnAdd | orderLocation                  | Json        |
	  | ColumnAdd | orderContentTypeId             | Int         |
	  | ColumnAdd | orderDetails                   | Json        |
	  | ColumnAdd | partner_firstName              | String      |
	  | ColumnAdd | partner_lastName               | String      |
	  | ColumnAdd | partner_email                  | String      |
	  | ColumnAdd | partner_latitude               | Double      |
	  | ColumnAdd | partner_longitude              | Double      |
	  | ColumnAdd | partner_imageUrl               | String      |
	  | ColumnAdd | partner_online                 | Bool        |
	  | ColumnAdd | partner_userStatus             | String      |
	  | ColumnAdd | partner_statusMessage          | String      |
	  | ColumnAdd | partner_ratingAvg              | Double      |
	Then "client1" the following data is received eventually on report "orderRequest"
	  | ~Action     | contentTypeContentTypeId | contentTypeDestination | contentTypeFromTime | contentTypeName | contentTypeNoItems | contentTypeNoPeople | contentTypeOrigin | contentTypePricingStrategy | contentTypeRootProductCategory | contentTypeTillTime | created       | currentDistance    | customerUserId | deliveryId                           | destinationCity | destinationFlatNumber | destinationLatitude | destinationLine1  | destinationLongitude | destinationPostCode | distance | duration | email | firstName | fixedPriceValue | from          | id | imageUrl                                                                                                    | initiatedByMe | isFixedPrice | lastName | latitude | longitude | market | notes        | notional | online | orderId                              | originCity     | originFlatNumber | originLatitude | originLine1         | originLongitude | originPostCode | path  | productId  | productImageUrl | productName   | productProductId | range | rank | status | till                 | totalPrice |
