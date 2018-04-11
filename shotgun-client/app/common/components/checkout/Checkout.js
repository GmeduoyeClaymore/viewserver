import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ContentTypeSelect from './ContentTypeSelect';
import DeliveryOptions from './DeliveryOptions';
import ItemDetails from './ItemDetails';
import OrderConfirmation from './OrderConfirmation';
import ProductCategoryList from './ProductCategoryList';
import FlatProductCategoryList from './FlatProductCategoryList';
import UsersForProductMap from './UsersForProductMap';
import ProductList from './ProductList';
import AddressLookup from 'common/components/maps/AddressLookup';
import {Route, ReduxRouter, withExternalState, removeProperties} from 'custom-redux';
import {INITIAL_STATE} from './CheckoutInitialState';
import Logger from 'common/Logger';
import * as ContentTypes from 'common/constants/ContentTypes';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('NavigationPath').
    personell(['ContentTypeSelect','ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    rubbish(['ContentTypeSelect','FlatProductCategoryList', 'ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    skip(['ContentTypeSelect','ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation']).
    delivery(['ContentTypeSelect','ProductList', 'DeliveryMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    hire(['ContentTypeSelect','ProductCategoryList', 'DeliveryMap', 'DeliveryOptions', 'OrderConfirmation']);

class Checkout extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'customerCheckout';
  
 
  constructor(props){
    super(props);
    const {history, path} = props;
    Logger.info('Creating checkout component');
    this.getNext = this.getNext.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentWillUnmount(){
    Logger.info("Throwing checkout away as for some reason we don't think we need it anymore ");
  }

  getNext(currentPath){
    if(this.resources && this.resources.NavigationPath){
      const navigationPath = this.resources.NavigationPath;
      const currentIndex = navigationPath.indexOf(currentPath);
      const {path} = this.props;
      if(!!~currentIndex){
        return `${path}/${navigationPath[currentIndex+1]}`;
      }
    }
  }

  render() {
    const {resetComponentState: resetParentComponentState} = this.props;
    const customerProps = {...this.props, stateKey: Checkout.stateKey, resetParentComponentState};
    const rest = removeProperties(customerProps, ['stateKey', 'setState', 'setStateWithPath', 'parentPath']);
    const {path} = this.props;
    const {getNext} = this;
    return <ReduxRouter  name="CheckoutRouter" {...rest} path={path} defaultRoute={'ContentTypeSelect'}>
      <Route stateKey={Checkout.stateKey} path={'ContentTypeSelect'} exact component={ContentTypeSelect} next={getNext('ContentTypeSelect')}/>
      <Route stateKey={Checkout.stateKey} transition='left' path={'DeliveryMap'} exact component={DeliveryMap} next={getNext('DeliveryMap')}/>
      <Route stateKey={Checkout.stateKey} transition='left' path={'AddressLookup'} exact component={AddressLookup} next={getNext('AddressLookup')} />
      <Route stateKey={Checkout.stateKey} transition='left' path={'DeliveryOptions'} exact component={DeliveryOptions} next={getNext('DeliveryOptions')} />
      <Route stateKey={Checkout.stateKey} transition='left' path={'ProductCategoryList'} exact component={ProductCategoryList}  next={getNext('ProductCategoryList')} />
      <Route stateKey={Checkout.stateKey} transition='left' path={'FlatProductCategoryList'} exact component={FlatProductCategoryList} next={getNext('FlatProductCategoryList')}  />
      <Route stateKey={Checkout.stateKey} transition='left' path={'ProductList'} exact component={ProductList}  next={getNext('ProductList')} />
      <Route stateKey={Checkout.stateKey} transition='left' path={'ItemDetails'} exact component={ItemDetails}  next={getNext('ItemDetails')} />
      <Route stateKey={Checkout.stateKey} transition='left' path={'OrderConfirmation'} exact component={OrderConfirmation} next={getNext('ItemDetails')}/>
      <Route stateKey={Checkout.stateKey} transition='left' path={'UsersForProductMap'} exact component={UsersForProductMap} next={getNext('UsersForProductMap')} />
    </ReduxRouter>;
  }
}

export default withExternalState()(Checkout);
