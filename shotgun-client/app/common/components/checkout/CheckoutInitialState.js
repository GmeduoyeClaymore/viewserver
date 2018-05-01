export const DELIVERY_ORDER_INITIAL_STATE =  {
  selectedContentType: {
    contentTypeId: 1,
    name: 'Delivery',
    rootProductCategory: '1Vans',
    pricingStrategy: 'JOURNEY_DISTANCE',
    description: 'Schedule deliveries in an instant',
    hasOrigin: true,
    hasDestination: true,
    hasStartTime: true
  },
  order: {
    orderProduct: {
      dimension_productId: 4,
      productId: '4Luton',
      name: 'Box/Luton Van',
      description: 'A Luton or box van. Ideal for moving a two bedroom flat or small house with up top 40 boxes of personal belongings, dining table and chairs, two and three seat sofas, two double beds, wardrobes, chest of drawers and all kitchen white goods. Avg loading dimensions 3.9x2.2x2m',
      categoryId: '1Vans',
      price: 5000,
      imageUrl: 'luton-lorry',
      rank: 0,
      key: 0,
      rowId: 0
    },
    orderContentType: 1,
    title: 'Box/Luton Van',
    productId: '4Luton',
    amount: 5000,
    origin: {
      line1: '12 Cromwell Rd',
      city: 'London',
      postCode: 'SW5 0SE',
      country: 'United Kingdom',
      googlePlaceId: 'ChIJvTu8sPQPdkgRqaph3ybp8KE',
      latitude: 51.494426,
      longitude: -0.1927663
    },
    destination: {
      line1: '333 North End Rd',
      city: 'London',
      postCode: 'SW6 1NN',
      country: 'United Kingdom',
      googlePlaceId: 'ChIJJUyCW5APdkgRcCNMMsNN5sY',
      latitude: 51.4849723,
      longitude: -0.2020104
    },
    distanceAndDuration: {
      distance: 1829,
      duration: 433
    },
    requiredDate: '2018-05-01T11:44:14.620Z',
    description: 'Sefsefsefsef'
  },
  payment: {
    paymentId: 'card_1BZjOtJf2h7PvwlulrG7Lrbx',
    brand: 'Visa',
    last4: '4242'
  }
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
