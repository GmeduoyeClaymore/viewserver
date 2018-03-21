import * as ContentTypes from 'common/constants/ContentTypes';

export default class ContentTypeNavigationStrategy{
  constructor(history){
    this.history = history;
    if (!this.history){
      throw new Error('History must be set');
    }
    this.pathIndex = -1;
    this.contentTypePaths = {};
    this.contentTypePaths[ContentTypes.DELIVERY] = ['VehicleSelect', 'DeliveryMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.RUBBISH] = ['FlatProductCategoryList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.SKIP] = ['ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.PERSONELL] = ['ProductCategoryList', 'UsersForProductMap', 'DeliveryOptions', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.HIRE] = ['ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation'];
    this.init = this.init.bind(this);
    this.next = this.next.bind(this);
    this.prev = this.prev.bind(this);
    this.goToIndex = this.goToIndex.bind(this);
  }


  init(contentType){
    this.contentType = contentType;
    this.pathIndex = -1;
    if (!this.contentType){
      throw new Error('Content type must be set');
    }
  }

  next(payload){
    if (!this.contentType){
      throw new Error('Content type must be set');
    }
    const paths = this.contentTypePaths[this.contentType];
    if (!paths){
      throw new Error('Unable to find paths for content type ' + this.contentType);
    }
    this.goToIndex(paths, payload, this.pathIndex + 1);
  }

  prev(payload){
    if (!this.contentType){
      throw new Error('Content type must be set');
    }
    const paths = this.contentTypePaths[this.contentType];
    if (!paths){
      throw new Error('Unable to find paths for content type ' + this.contentType);
    }

    this.goToIndex(paths, payload, this.pathIndex - 1);
  }

  goToIndex(paths, payload, newIndex){
    if (newIndex >= paths.length){
      throw new Error(`Unable to find path index ${newIndex} for content type ${contentType}`);
    }
   
    if (newIndex == -1){
      this.history.push('/Customer/Checkout/ProductSelect');
    } else {
      const nextPath = paths[newIndex];
      this.history.push(`/Customer/Checkout/${nextPath}`, payload);
    }
    this.pathIndex = newIndex;
  }
}
