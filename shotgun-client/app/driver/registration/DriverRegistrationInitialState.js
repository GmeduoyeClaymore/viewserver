/*export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    type: 'driver',
    password: undefined,
    dob: '1982-02-03'
  },
  address: {
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
    email: 'test@test.com',
    contactNo: '07733362799',
    type: 'driver',
    password: 'password',
    dob: '1982-02-03'
  },
  vehicle: {
    registrationNumber: 'nx01ert',
    colour: 'blue',
    make: 'Ford',
    model: 'Transit',
    numAvailableForOffload: 0,
    dimensions: {
      height: 2000,
      width: 2000,
      length: 2000,
      weight: 1000
    }
  },
  address: {
    line1: 'line1',
    city: 'London',
    postCode: 'SW178RS'
  },
  bankAccount: {
    accountNumber: '00012345',
    sortCode: '108800'
  }
};


