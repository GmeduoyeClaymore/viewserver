export const INITIAL_STATE = {
  totalPrice: undefined,
  price: undefined,
  selectedProduct: undefined,
  showAll: true,
  orderItem: {
    productId: undefined,
    notes: undefined,
    imageUrl: undefined,
    imageData: undefined
  },
  selectedContentType: {
    contentTypeId: undefined,
    name: undefined,
    origin: undefined,
    destination: undefined,
    fromTime: undefined,
    tillTime: undefined,
    noItems: undefined,
    rootProductCategory: undefined,
    pricingStrategy: undefined
  },
  deliveryUser: undefined,
  delivery: {
    from: undefined,
    noRequiredForOffload: 0,
    deliveryId: undefined,
    distance: undefined,
    isFixedPrice: false,
    fixedPriceValue: undefined,
    duration: undefined,
    origin: {
      flatNumber: undefined,
      line1: undefined,
      city: undefined,
      postCode: undefined,
      googlePlaceId: undefined,
      latitude: undefined,
      longitude: undefined
    },
    destination: {
      flatNumber: undefined,
      line1: undefined,
      city: undefined,
      postCode: undefined,
      googlePlaceId: undefined,
      latitude: undefined,
      longitude: undefined
    }
  },
  payment: {
    paymentId: undefined
  }
};

/*
export const INITIAL_STATE = {
  orderItem: {
    productId: '1SmallVan',
    contentTypeId: 1,
    notes: 'some notes are here \n there are a lot of them',
    imageData: undefined,
    imageUrl: undefined,
  },
  selectedContentType: {
    contentTypeId: 1,
    name: 'Delivery',
    origin: true,
    destination: true,
    noPeople: true,
    fromTiem: true,
    noItems: true,
    rootProductCategory: '1Vans',
    pricingStrategy: 'JOURNEY_DISTANCE',
    description: 'Schedule deliveries in an instant'
  },
  delivery: {
    from: new Date(),
    noRequiredForOffload: 0,
    deliveryId: undefined,
    origin: {
      line1: '12 Kinnoul Rd',
      city: 'London',
      postCode: 'SE12 4RT',
      googlePlaceId: 'EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL',
      latitude: 51.4857236,
      longitude: -0.2123406
    },
    destination: {
      flatNumber: undefined,
      line1: undefined,
      city: undefined,
      postCode: undefined,
      googlePlaceId: undefined,
      latitude: undefined,
      longitude: undefined
    }
  },
  payment: {
    paymentId: '12345'
  }
};
*/
