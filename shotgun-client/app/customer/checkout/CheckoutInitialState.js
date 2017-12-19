export const INITIAL_STATE = {
  orderItem: {
    productId: undefined,
    notes: undefined,
    imageUrl: undefined,
    imageData: undefined
  },
  delivery: {
    eta: 72,
    noRequiredForOffload: 0,
    vehicleTypeId: undefined,
    deliveryId: undefined,
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
    notes: 'some notes are here \n there are a lot of them',
    imageData: undefined,
    imageUrl: undefined,
    productId: 'PROD_Disposal',
  },
  delivery: {
    eta: 72,
    noRequiredForOffload: 0,
    vehicleTypeId: '12323232',
    deliveryId: undefined,
    origin: {
      line1: '12 Kinnoul Rd',
      city: 'London',
      postCode: 'SE124RT',
      googlePlaceId: 'EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL',
      latitude: 51.4857236,
      longitude: -0.2123406
    },
    destination: {
      line1: '129 Drakefield Rd',
      city: 'London',
      postCode: 'SW178RS',
      googlePlaceId: 'ChIJI6Q-6tkFdkgR5YDG0_IWuvw',
      latitude: 51.4341614,
      longitude: -0.1523323
    }
  },
  payment: {
    paymentId: '12345'
  }
};
*/
