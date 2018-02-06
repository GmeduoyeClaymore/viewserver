export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    password: undefined,
    type: 'customer'
  },
  deliveryAddress: {
    line1: undefined,
    line2: undefined,
    city: undefined,
    country: undefined,
    postCode: undefined,
    isDefault: true
  }
};

/*export const INITIAL_STATE = {
  user: {
    firstName: 'Paul',
    lastName: 'Graves',
    email: 'test5@test.com',
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
    googlePlaceId: '123456',
    latitude: 15.1,
    longitude: 16.3,
    isDefault: true
  }
};*/

