import Immutable from 'seamless-immutable';

export const INITIAL_STATE = Immutable({
  product: {
    products: [],
    categories: [],
    status: {
      error: '',
      busy: false
    }
  }
});
