export const INITIAL_STATE = {
  delivery: {
    eta: 72,
    deliveryType: 'ROADSIDE',
    deliveryId: undefined,
    origin: {
      name: undefined,
      place_id: undefined,
      location: {
        latitude: undefined,
        longitude: undefined
      }
    },
    destination: {
      name: undefined,
      place_id: undefined,
      location: {
        latitude: undefined,
        longitude: undefined
      }
    }
  },
  payment: {
    paymentId: undefined
  }
};

/*
export const INITIAL_STATE = {
  delivery: {
    isDeliveryRequired: true,
    eta: 72,
    deliveryType: 'ROADSIDE',
    deliveryId: undefined,
    origin: {
      name: "12 Kinnoul Rd",
      place_id:  "EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL",
      location: {
        latitude: 51.4857236,
        longitude:  -0.2123406
      }
    },
    destination: {
      name:  "129 Drakefield Rd",
      place_id:  "ChIJI6Q-6tkFdkgR5YDG0_IWuvw",
      location: {
        latitude:  51.4341614,
        longitude:  -0.1523323
      }
    }
  },
  payment: {
    paymentId: undefined
  }
};*/
