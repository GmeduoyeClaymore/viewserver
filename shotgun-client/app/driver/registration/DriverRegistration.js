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
import AddPropsToRoute from 'common/AddPropsToRoute';

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
    return <ReduxRouter {...driverRegistrationProps} defaultRoute='/Driver/Registration/DriverRegistrationLanding'>
      <Route path={'/Driver/Registration/DriverRegistrationLanding'} exact component={AddPropsToRoute(DriverRegistrationLanding, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/Login'} exact component={AddPropsToRoute(DriverLogin, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/UserDetails'} exact component={AddPropsToRoute(UserDetails, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/AddressDetails'} exact component={AddPropsToRoute(AddressDetails, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/AddressLookup'} exact component={AddPropsToRoute(AddressLookup, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/BankAccountDetails'} exact component={AddPropsToRoute(BankAccountDetails, driverRegistrationProps)}/>
      <Route path={'/Driver/Registration/DriverAccountType'} exact component={AddPropsToRoute(DriverAccountType, driverRegistrationProps)}/>
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

