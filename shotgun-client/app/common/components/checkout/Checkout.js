import React, {Component} from 'react';
import DeliveryMap from './DeliveryMap';
import ContentTypeSelect from './ContentTypeSelect';
import DeliveryOptions from './DeliveryOptions';
import VehicleSelect from './VehicleSelect';
import ItemDetails from './ItemDetails';
import OrderConfirmation from './OrderConfirmation';
import ProductCategoryList from './ProductCategoryList';
import FlatProductCategoryList from './FlatProductCategoryList';
import UsersForProductMap from './UsersForProductMap';
import ProductList from './ProductList';
import AddressLookup from 'common/components/maps/AddressLookup';
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './CheckoutInitialState';
import ContentTypeNavigationStrategy from './ContentTypeNavigationStrategy';
import { withRouter } from 'react-router';
import {connect} from 'custom-redux';

class Checkout extends Component {
  constructor(props){
    super(props);
    this.state = INITIAL_STATE;
    const {history, match} = props;
    this.navigationStrategy = new ContentTypeNavigationStrategy(history, match.path);
  }

  render() {
    const {navigationStrategy} = this;
    const customerProps = {navigationStrategy, ...this.props};
    const {match = {}} = this.props;
    return <Switch>
      <Route path={`${match.path}/ContentTypeSelect`} exact render={() => <ContentTypeSelect {...customerProps} context={this}/>} />
      <Route path={`${match.path}/DeliveryMap`} exact render={() => <DeliveryMap {...customerProps} context={this}/>} />
      <Route path={`${match.path}/DeliveryOptions`} exact render={() => <DeliveryOptions {...customerProps} context={this}/>} />
      <Route path={`${match.path}/VehicleSelect`} exact render={() => <VehicleSelect {...customerProps} context={this}/>} />
      <Route path={`${match.path}/ProductCategoryList`} exact render={() => <ProductCategoryList {...customerProps} context={this}/>} />
      <Route path={`${match.path}/FlatProductCategoryList`} exact render={() => <FlatProductCategoryList {...customerProps} context={this}/>} />
      <Route path={`${match.path}/ProductList`} exact render={() => <ProductList {...customerProps} context={this}/>} />
      <Route path={`${match.path}/ProductDetails`} exact render={() => <ProductDetails {...customerProps} context={this}/>} />
      <Route path={`${match.path}/ItemDetails`} exact render={() => <ItemDetails {...customerProps} context={this}/>} />
      <Route path={`${match.path}/AddressLookup`} exact render={() => <AddressLookup {...customerProps} context={this}/>} />
      <Route path={`${match.path}/OrderConfirmation`} exact render={() => <OrderConfirmation {...customerProps} context={this}/>} />
      <Route path={`${match.path}/UsersForProductMap`} exact render={() => <UsersForProductMap {...customerProps} context={this}/>} />
      <Redirect to={`${match.path}/ContentTypeSelect`}/>
    </Switch>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {history: parentHistory, match: parentMatch} = initialProps;

  return {
    ...initialProps,
    parentHistory,
    parentMatch
  };
};

export default connect(
  mapStateToProps
)(withRouter(Checkout));
