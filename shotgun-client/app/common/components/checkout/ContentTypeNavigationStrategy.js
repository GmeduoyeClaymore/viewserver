import * as ContentTypes from 'common/constants/ContentTypes';
import Logger from 'common/Logger';
const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('NavigationPath').
    personell(['ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    rubbish(['FlatProductCategoryList', 'ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    skip(['ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation']).
    delivery(['ProductList', 'DeliveryMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    hire(['ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation']);


export default class ContentTypeNavigationStrategy{
  constructor(history, path, defaultPath){
    this.history = history;
    this.defaultPath = defaultPath;
    this.path = path;
    if (!this.history){
      throw new Error('History must be set');
    }
    if (!this.defaultPath){
      throw new Error('defaultPath must be set');
    }
    this.pathIndex = -1;
    this.init = this.init.bind(this);
    this.next = this.next.bind(this);
    this.prev = this.prev.bind(this);
    this.goToIndex = this.goToIndex.bind(this);
  }

  init(contentTypeId){
    this.contentTypeId = contentTypeId;
    this.paths = resourceDictionary.resolveInternal(contentTypeId).NavigationPath;
    this.pathIndex = -1;
  }

  next(payload){
    const {paths} = this;
    if (!paths){
      throw new Error('Unable to find paths ensure strategy has been initialized');
    }
    this.goToIndex(paths, payload, this.pathIndex + 1, true);
  }

  prev(payload){
    const {paths} = this;
    if (!paths){
      throw new Error('Unable to find paths ensure strategy has been initialized');
    }

    this.goToIndex(paths, payload, this.pathIndex - 1, false);
  }

  goToIndex(paths, payload, newIndex, increment){
    if (newIndex >= paths.length){
      Logger.warning(`Unable to find path index ${newIndex} for content type ${this.contentTypeId}`);
      return;
    }
   
    if (newIndex == -1){
      this.history.replace(`${this.path}/${this.defaultPath}`);
    } else {
      const nextPath = paths[newIndex];
      if (increment){
        this.history.push(`${this.path}/${nextPath}`, payload);
      } else {
        this.history.replace({pathname: `${this.path}/${nextPath}`, isReverse: true}, payload);
      }
    }
    this.pathIndex = newIndex;
  }
}
