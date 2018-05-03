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
import Logger from 'common/Logger';
import * as ContentTypes from 'common/constants/ContentTypes';
import {LoadingScreen} from 'common/components';
import {isAnyOperationPending} from 'common/dao';

class Checkout extends Component {
  static stateKey = 'customerCheckout';
  //static InitialState = PERSONELL_ORDER_INITIAL_STATE;

  constructor(props){
    super(props);
    Logger.info('Creating checkout component');
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentWillUnmount(){
    Logger.info("Throwing checkout away as for some reaso`n we don't think we need it anymore ");
  }

  getNext = (currentPath) => {
    if (this.resources && this.resources.NavigationPath){
      const navigationPath = this.resources.NavigationPath;
      const currentIndex = navigationPath.indexOf(currentPath);
      const {path} = this.props;
      if (!!~currentIndex){
        return `${path}/${navigationPath[currentIndex + 1]}`;
      }
    }
  }

  render() {
    const {path, busy} = this.props;
    const customerProps = {...this.props, stateKey: Checkout.stateKey};
    const rest = removeProperties(customerProps, ['stateKey', 'setState', 'setStateWithPath', 'parentPath']);
    const {getNext} = this;

    return busy ? <LoadingScreen text="Loading"/> :
      <ReduxRouter  name="CheckoutRouter" {...rest} path={path} defaultRoute={'ContentTypeSelect'}>
        <Route stateKey={Checkout.stateKey} path={'ContentTypeSelect'} exact component={ContentTypeSelect} next={getNext('ContentTypeSelect')}/>
        <Route stateKey={Checkout.stateKey} transition='left' path='DeliveryMap' exact component={DeliveryMap} next={getNext('DeliveryMap')}/>
        <Route stateKey={Checkout.stateKey} transition='left' path='AddressLookup' exact component={AddressLookup} next={getNext('AddressLookup')} />
        <Route stateKey={Checkout.stateKey} transition='left' path='DeliveryOptions' exact component={DeliveryOptions} next={getNext('DeliveryOptions')} />
        <Route stateKey={Checkout.stateKey} transition='left' path='ProductCategoryList' exact component={ProductCategoryList}  next={getNext('ProductCategoryList')} />
        <Route stateKey={Checkout.stateKey} transition='left' path='FlatProductCategoryList' exact component={FlatProductCategoryList} next={getNext('FlatProductCategoryList')}  />
        <Route stateKey={Checkout.stateKey} transition='left' path='ProductList' exact component={ProductList}  next={getNext('ProductList')} />
        <Route stateKey={Checkout.stateKey} transition='left' path='ItemDetails' exact component={ItemDetails}  next={getNext('ItemDetails')} />
        <Route stateKey={Checkout.stateKey} transition='left' path='OrderConfirmation' exact component={OrderConfirmation} next={getNext('ItemDetails')}/>
        <Route stateKey={Checkout.stateKey} transition='left' path='UsersForProductMap' exact component={UsersForProductMap} next={getNext('UsersForProductMap')} />
      </ReduxRouter>;
  }
}

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('NavigationPath').
    personell(['ContentTypeSelect','ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    rubbish(['ContentTypeSelect','FlatProductCategoryList','ProductList', 'UsersForProductMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    skip(['ContentTypeSelect','FlatProductCategoryList','ProductList', 'UsersForProductMap', 'DeliveryOptions', 'OrderConfirmation']).
    delivery(['ContentTypeSelect','ProductList', 'DeliveryMap', 'DeliveryOptions', 'ItemDetails', 'OrderConfirmation']).
    hire(['ContentTypeSelect','FlatProductCategoryList','ProductList', 'UsersForProductMap', 'DeliveryOptions', 'OrderConfirmation']);
/*eslint-enable */

const mapStateToProps = (state, nextOwnProps) => {
  return {
    ...nextOwnProps,
    busy: isAnyOperationPending(state, [{ paymentDao: 'getPaymentCards' }])
  };
};

export default withExternalState(mapStateToProps)(Checkout);
