export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    type: 'driver',
    password: undefined,
    dob: undefined,
    imageData: undefined,
    range: 50
  },
  deliveryAddress: {
    flatNumber: undefined,
    line1: undefined,
    city: undefined,
    postCode: undefined,
    googlePlaceId: undefined,
    latitude: undefined,
    longitude: undefined,
    isDefault: true
  }
};

/*
export const INITIAL_STATE = {
  user: {
    firstName: 'paul',
    lastName: 'graves',
    email: `test${Math.random() * 1000}@test.com`,
    contactNo: '07733362799',
    type: 'driver',
    password: 'password',
    dob: '1982-02-03',
    range: 10
  },
  deliveryAddress: {
    line1: '129 Drakefield Road',
    line2: 'Tooting',
    city: 'London',
    country: 'UK',
    postCode: 'SW17 8RS',
    googlePlaceId: '123456',
    latitude: 15.1,
    longitude: 16.3,
    isDefault: true
  }
};
*/

