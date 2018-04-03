import React, {Component} from 'react';
import {withExternalState, Route, ReduxRouter} from 'custom-redux';
import UserDetails from 'common/registration/UserDetails';
import DriverAccountType from './DriverAccountType';
import DriverLogin from './DriverLogin';
import BankAccountDetails from './BankAccountDetails';
import DriverRegistrationLanding from './DriverRegistrationLanding';
import AddressDetails from 'common/registration/AddressDetails';
import AddressLookup from 'common/components/maps/AddressLookup';
import {unregisterAllDaos, registerNakedDao, register} from 'common/actions/CommonActions';
import DriverDao from 'driver/dao/DriverDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import {INITIAL_STATE} from './DriverRegistrationInitialState';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import ProductDao from 'common/dao/ProductDao';

class DriverRegistration extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'driverRegistration';
  constructor() {
    super();
  }

  componentDidMount(){
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    registerNakedDao(dispatch, new DriverDao(client));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new ProductCategoryDao(client));
    register(dispatch, new UserRelationshipDao(client));
    register(dispatch, new ProductDao(client), {});
  }

  render() {
    const driverRegistrationProps = {...this.props, stateKey: DriverRegistration.stateKey};
    const {path} = this.props;
    return <ReduxRouter  name="DriverRegistrationRouter" {...driverRegistrationProps} defaultRoute={`${path}/DriverRegistrationLanding`}>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/DriverRegistrationLanding`} exact component={DriverRegistrationLanding}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/Login`} exact component={DriverLogin}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/UserDetails`} next={`${path}/AddressDetails`} exact component={UserDetails}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/AddressDetails`} next={`${path}/BankAccountDetails`} exact component={AddressDetails}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/AddressLookup`} exact component={AddressLookup}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/BankAccountDetails`} exact component={BankAccountDetails}/>
      <Route stateKey={DriverRegistration.stateKey} path={`${path}/DriverAccountType`} exact component={DriverAccountType}/>
    </ReduxRouter>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    ...nextOwnProps
  };
};

export default withExternalState(mapStateToProps)(DriverRegistration);

