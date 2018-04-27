/*export const INITIAL_STATE = {
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
  address: {
    flatNumber: undefined,
    line1: undefined,
    city: undefined,
    postCode: undefined
  },
  vehicle: {
    registrationNumber: undefined,
    colour: undefined,
    make: undefined,
    model: undefined,
    numAvailableForOffload: undefined,
    dimensions: {
      height: undefined,
      width: undefined,
      length: undefined,
      weight: undefined
    }
  }
};*/

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
  address: {
    line1: '129 Drakefield Road',
    line2: 'Tooting',
    city: 'London',
    country: 'UK',
    postCode: 'SW17 8RS',
    googlePlaceId: '123456',
    latitude: 15.1,
    longitude: 16.3,
    isDefault: true
  },
  vehicle: {
    registrationNumber: 'YA61AYB',
    colour: undefined,
    make: undefined,
    model: undefined,
    numAvailableForOffload: undefined,
    dimensions: {
      height: undefined,
      width: undefined,
      length: undefined,
      weight: undefined
    }
  }
};

