/*
export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    password: undefined,
    type: 'customer',
  },
  deliveryAddress: {
    line1: undefined,
    line2: undefined,
    city: undefined,
    country: undefined,
    postcode: undefined,
    isDefault: true
  }
};
*/

export const INITIAL_STATE = {
  user: {
    firstName: 'Paul',
    lastName: 'Graves',
    email: 'test@test.com',
    contactNo: '07733362799',
    type: 'customer',
    password: 'test',
  },
  deliveryAddress: {
    line1: '129 Drakefield Road',
    line2: 'Tooting',
    city: 'London',
    country: 'UK',
    postCode: 'SW17 8RS',
    googlePlaceId: '',
    latitude: 0,
    longitude: 0,
    isDefault: true
  }
};

