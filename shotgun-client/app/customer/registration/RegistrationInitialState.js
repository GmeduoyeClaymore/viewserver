export const INITIAL_STATE = {
  customer: {
    firstName: undefined,
    lastName: undefined,
    email: undefined,
    contactNo: undefined
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
  },
  status: {
    error: '',
    busy: false
  }
};
