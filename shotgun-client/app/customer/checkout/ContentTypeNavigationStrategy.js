import * as ContentTypes from './ContentTypes';

export default class ContentTypeNavigationStrategy{
  constructor(history){
    this.history = history;
    if (!this.history){
      throw new Error('History must be set');
    }
    this.pathIndex = -1;
    this.contentTypePaths = {};
    this.contentTypePaths[ContentTypes.DELIVERY] = ['DeliveryMap', 'DeliveryOptions', 'VehicleDetails', 'ItemDetails', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.RUBBISH] = ['DeliveryMap', 'ProductCategoryList', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.SKIP] = ['DeliveryMap', 'ProductCategoryList', 'DeliveryOptions', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.PERSONELL] = ['DeliveryMap', 'ProductCategoryList', 'DeliveryOptions', 'OrderConfirmation'];
    this.contentTypePaths[ContentTypes.HIRE] = ['DeliveryMap', 'ProductCategoryList', 'DeliveryOptions', 'OrderConfirmation'];
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
