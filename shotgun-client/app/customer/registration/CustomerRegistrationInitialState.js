/*export const INITIAL_STATE = {
  user: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined,
    type: 'customer'
  },
  deliveryAddress: {
    line1: undefined,
    line2: undefined,
    city: undefined,
    country: undefined,
    postcode: undefined,
    isDefault: true
  },
  paymentCard: {
    cardNumber: undefined,
    expiryMonth: undefined,
    expiryYear: undefined,
    cvv: undefined,
    isDefault: true
  }
};*/


export const INITIAL_STATE = {
  user: {
    firstName: 'Paul',
    lastName: 'Graves',
    email: 'test@test.com',
    contactNo: '07733362799',
    type:'customer'
  },
  deliveryAddress: {
    line1: '129 Drakefield Road',
    line2: 'Tooting',
    city: 'London',
    country: 'UK',
    postcode: 'SW17 8RS',
    isDefault: true
  },
  paymentCard: {
    cardNumber: '1234567890123456',
    expiryMonth: '01',
    expiryYear: '19',
    cvv: '123'
  }
};

