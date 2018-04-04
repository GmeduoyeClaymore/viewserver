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
import ContentTypeNavigationStrategy from './ContentTypeNavigationStrategy';
import Logger from 'common/Logger';
class Checkout extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'customerCheckout';
 
  constructor(props){
    super(props);
    const {history, path} = props;
    this.navigationStrategy = new ContentTypeNavigationStrategy(history, path, 'ContentTypeSelect');
    Logger.info('Creating checkout component');
  }

  componentWillUnmount(){
    Logger.info("Throwing checkout away as for some reason we don't think we need it anymore ");
  }

  render() {
    const {navigationStrategy} = this;
    const {resetComponentState: resetParentComponentState} = this.props;
    const customerProps = {navigationStrategy, ...this.props, stateKey: Checkout.stateKey, resetParentComponentState};
    const rest = removeProperties(customerProps, ['stateKey', 'setState', 'setStateWithPath', 'parentPath']);
    const {path} = this.props;
    return <ReduxRouter  name="CheckoutRouter" {...rest} path={path} defaultRoute={'ContentTypeSelect'}>
      <Route stateKey={Checkout.stateKey} path={'ContentTypeSelect'} exact component={ContentTypeSelect} />
      <Route stateKey={Checkout.stateKey} path={'DeliveryMap'} exact component={DeliveryMap}/>
      <Route stateKey={Checkout.stateKey} path={'AddressLookup'} exact component={AddressLookup} />
      <Route stateKey={Checkout.stateKey} path={'DeliveryOptions'} exact component={DeliveryOptions} />
      <Route stateKey={Checkout.stateKey} path={'ProductCategoryList'} exact component={ProductCategoryList} />
      <Route stateKey={Checkout.stateKey} path={'FlatProductCategoryList'} exact component={FlatProductCategoryList} />
      <Route stateKey={Checkout.stateKey} path={'ProductList'} exact component={ProductList} />
      <Route stateKey={Checkout.stateKey} path={'ItemDetails'} exact component={ItemDetails} />
      <Route stateKey={Checkout.stateKey} path={'OrderConfirmation'} exact component={OrderConfirmation} />
      <Route stateKey={Checkout.stateKey} path={'UsersForProductMap'} exact component={UsersForProductMap} />
    </ReduxRouter>;
  }
}

export default withExternalState()(Checkout);
