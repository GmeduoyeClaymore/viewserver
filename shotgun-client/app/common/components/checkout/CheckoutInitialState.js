import {PaymentTypes} from 'common/constants/PaymentTypes';

export const DELIVERY_ORDER_INITIAL_STATE =  {
  selectedContentType: undefined,
  order: {
    orderProduct: undefined,
    orderContentTypeId: 1,
    origin: undefined,
    destination: undefined,
    distanceAndDuration: undefined,
    paymentType: PaymentTypes.FIXED
  },
  payment: undefined
};

/*
export const RUBBISH_ORDER_INITIAL_STATE =  {
  selectedContentType: undefined,
  order: {
    orderProduct: undefined,
    orderContentTypeId: 2,
    origin: undefined,
    destination: undefined,
    distanceAndDuration: undefined,
    paymentType: PaymentTypes.FIXED
  },
  payment: undefined
};
*/

export const PERSONELL_ORDER_INITIAL_STATE =  {
  selectedContentType: undefined,
  order: {
    paymentType: 'Fixed',
    orderProduct: undefined,
    orderContentTypeId: 5,
    origin: undefined,
    destination: undefined,
    distanceAndDuration: undefined,
    paymentType: PaymentTypes.FIXED
  },
  payment: undefined
};

/*
export const PERSONELL_ORDER_INITIAL_STATE =  {
  selectedContentType: {
    contentTypeId: 5,
    name: 'Trades People',
    rootProductCategory: '1Workers',
    pricingStrategy: 'DURATION',
  },
  order: {
    orderProduct: {
      dimension_productId: 5,
      productId: 'Carpenter',
      name: 'Carpenter',
      description: '',
      categoryId: '1Workers',
      price: 15000,
    },
    paymentType: PaymentTypes.FIXED,
    orderContentTypeId: 5,
    origin: {
      googlePlaceId: 'ChIJ64Z3QcUEdkgRu_XB2S70e60',
      city: 'London',
      latitude: 51.5033635,
      postCode: 'SW1A 2AB',
      line1: '11 Downing Street',
      longitude: -0.1276248
    },
    destination: undefined,
    distanceAndDuration: undefined
  },
  payment: undefined
};
*/


export const RUBBISH_ORDER_INITIAL_STATE =  {
  selectedContentType: {
    contentTypeId: 2,
    name: "Rubbish Collection",
    rootProductCategory: "1Rubbish",
    pricingStrategy: "FIXED",
    description: "Get rid of site rubbish now. Track that driver !!"
  },
  productCategory: {
    categoryId: "1Rubbish",
    category: "Rubbish",
    parentCategoryId: "NONE",
    level: 1,
    path: "1Rubbish"
  },
  order: {
    orderProduct: {
      dimension_productId: 16,
      productId: "HouseHoldThreeQuarterVan",
      name: "Household Three Quarter van",
      description: "750 kg",
      categoryId: "2HouseHoldWaste",
      price: 18000,
      rank: 1,
      key: 1,
      rowId: 1
    },
    orderContentTypeId: 2,
    origin: {
      googlePlaceId: "ChIJ64Z3QcUEdkgRu_XB2S70e60",
      city: "London",
      latitude: 51.5033635,
      postCode: "SW1A 2AB",
      line1: "11 Downing Street",
      longitude: -0.1276248
    },
    destination: undefined,
    distanceAndDuration: undefined
  },
  payment: undefined
};

/*export const INITIAL_STATE = {
  totalPrice: undefined,
  showAll: true,
  orderItem: {
    productId: '2MediumVan',
    contentTypeId: 1,
    quantity: 1,
    notes: 'some notes are here \n there are a lot of them',
    imageData: undefined,
    imageUrl: undefined,
  },
  selectedProduct: {
    productId: '2MediumVan',
    name: 'Medium Van',
    description: 'Ford Transit, Volkswagen Transporter or similar. Ideal for small moves and can generally take one or two peoples personal belongings of up to 30 to 40 assorted moving boxes, or a single large item of furniture such as a sofa or bed.',
    categoryId: '1Vans',
    price: 3000,
    imageUrl: 'mid-van',
    dimension_productId: '2MediumVan',
    prodConstantJoinCol: 1,
    rank: 2,
    key: 2
  },
  selectedContentType: {
    contentTypeId: 1,
    name: 'Delivery',
    rootProductCategory: '1Vans',
    pricingStrategy: 'JOURNEY_DISTANCE',
    description: 'Schedule deliveries in an instant',
    hasOrigin: true,
    hasDestination: true,
    hasStartTime: true,
    hasEndTime: undefined
  },
  delivery: {
    deliveryId: undefined,
    distance: 9487,
    duration: 1740,
    origin: {
      line1: '12 Kinnoul Rd',
      city: 'London',
      postCode: 'SE12 4RT',
      googlePlaceId: 'EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL',
      latitude: 51.4857236,
      longitude: -0.2123406
    },
    destination: {
      flatNumber: undefined,
      line1: '94 Oxford St',
      city: 'London',
      postCode: 'W1D 1BZ',
      googlePlaceId: 'ChIJl6cFYCsbdkgRcm8iIqkGTtE',
      latitude: 51.5161747,
      longitude: -0.1350527
    }
  },
  payment: {
    paymentId: undefined,
    brand: undefined,
    last4: undefined
  }
};*/

/*
export const INITIAL_STATE = {
  totalPrice: undefined,
  showAll: true,
  orderItem: {
    productId: 'BrickLayer',
    contentTypeId: 5,
    quantity: 1,
    notes: 'some notes are here \n there are a lot of them',
    imageData: undefined,
    imageUrl: undefined,
  },
  selectedProduct: {
    productId: 'BrickLayer',
    name: 'Brick Layer',
    description: 'Any one can lay a new brick the reclaimed ones are the charm. Give us four corners and well work wonders',
    categoryId: '1Workers',
    price: 15000,
    imageUrl: '',
    dimension_productId: 'BrickLayer',
    prodConstantJoinCol: 1,
    rank: 0,
    key: 0,
    rowId: 0,
  },
  selectedContentType: {
    contentTypeId: 5,
    name: 'Trades People',
    hasOrigin: true,
    hasStartTime: true,
    hasEndTime: true,
    rootProductCategory: '1Workers',
    pricingStrategy: 'JOB_DURATION',
    description: 'Need a plumber? Ground worker? Labourer? Electrician? Get one now ... and yes track his progress !!',
  },
  delivery: {
    deliveryId: undefined,
    origin: {
      line1: '12 Kinnoul Rd',
      city: 'London',
      postCode: 'SE12 4RT',
      googlePlaceId: 'EhwxMiBLaW5ub3VsIFJkLCBMb25kb24gVzYsIFVL',
      latitude: 51.4857236,
      longitude: -0.2123406
    },
    destination: {}
  },
  payment: {
    paymentId: undefined,
    brand: undefined,
    last4: undefined
  }
};
*/
