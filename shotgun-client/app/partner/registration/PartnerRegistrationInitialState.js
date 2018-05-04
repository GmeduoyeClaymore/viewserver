export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    type: 'partner',
    password: undefined,
    dob: undefined,
    imageData: undefined,
    range: 50
  },
  deliveryAddress: {
    flatNumber: undefined,
    line1: undefined,
    city: undefined,
    postCode: undefined
  },
  bankAccount: {
    accountNumber: undefined,
    sortCode: undefined
  },
  vehicle: {
    registrationNumber: undefined,
    colour: undefined,
    make: undefined,
    model: undefined,
    dimensions: {
      height: undefined,
      width: undefined,
      length: undefined,
      weight: undefined
    }
  }
};

/*
export const INITIAL_STATE = {
  user: {
    firstName: 'paul',
    lastName: 'graves',
    email: `test${Math.random() * 1000}@test.com`,
    contactNo: '07733362799',
    type: 'partner',
    password: 'password',
    dob: '1982-02-03',
    range: 10
  },
  deliveryAddress: {
    line1: '129 Drakefield Road',
    line2: 'Tooting',
    city: 'London',
    postCode: 'SW178RS'
  },
  
  bankAccount: {
    accountNumber: '00012345',
    sortCode: '108800'
  },

  vehicle: {
    registrationNumber: 'YA61AYB',
    colour: undefined,
    make: undefined,
    model: undefined,
    dimensions: {
      height: undefined,
      width: undefined,
      length: undefined,
      weight: undefined
    }
  }
};
*/
