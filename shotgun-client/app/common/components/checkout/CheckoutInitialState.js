export const INITIAL_STATE = {
  totalPrice: undefined,
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

/*export const INITIAL_STATE = {
  orderItem: {
    notes: 'some notes are here \n there are a lot of them',
    imageData: undefined,
    imageUrl: undefined,
    productId: 'PROD_Disposal',
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
};*/

/*export const INITIAL_STATE = {
  totalPrice: undefined,
  orderItem: {
    notes: 'this is example delivery data',
    imageData: undefined,
    imageUrl: undefined,
    productId: '1SmallVan',
  },
  selectedProduct: {
    productId: 'GroundWorker',
    name: 'Ground worker',
    description: 'These guys just love to dig. Just supply spades indicate where you want the hole then stand back.'
  },
  selectedContentType: {
    contentTypeId: 1,
    name: 'Personell',
    origin: true,
    destination: true,
    fromTime: true,
    tillTime: false,
    noItems: 1,
    rootProductCategory: '1Workers',
    pricingStrategy: 'DURATION'
  },
  delivery: {
    from: new Date(),
    distance: 2000,
    duration: 5000,
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
      line1: '129 Drakefield Rd',
      city: 'London',
      postCode: 'SW17 8RS',
      googlePlaceId: 'ChIJI6Q-6tkFdkgR5YDG0_IWuvw',
      latitude: 51.4341614,
      longitude: -0.1523323
    }
  },
  payment: {
    paymentId: '12345'
  }
};*/
