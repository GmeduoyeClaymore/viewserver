export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    type: 'driver'
  },
  vehicle: {
    registrationNumber: undefined,
    colour: 'fromAPI',
    make: 'fromAPI',
    model: 'fromAPI',
    vehicleTypeId: undefined
  }
};


/*
export const INITIAL_STATE = {
  user: {
    firstName: 'paul',
    lastName: 'graves',
    email: 'test@test.com',
    contactNo: '1234565656',
    type: 'driver'
  },
  vehicle: {
    registrationNumber: 'nx01ert',
    colour: 'fromAPI',
    make: 'fromAPI',
    model: 'fromAPI',
    vehicleTypeId: '30257d56-b35b-48ee-a40f-bb102c077ab9'
  }
};
*/
