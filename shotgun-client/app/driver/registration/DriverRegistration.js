import React, {Component} from 'react';
import {connect, withExternalState} from 'custom-redux';
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
import {Route, Redirect, Switch} from 'react-router-native';
import {INITIAL_STATE} from './DriverRegistrationInitialState';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import ProductDao from 'common/dao/ProductDao';
import { withRouter } from 'react-router';

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
    return <Switch>
      <Route path={'/Driver/Registration/DriverRegistrationLanding'} exact render={() => <DriverRegistrationLanding {...driverRegistrationProps}/>} />
      <Route path={'/Driver/Registration/Login'} exact render={() => <DriverLogin {...driverRegistrationProps}/>} />
      <Route path={'/Driver/Registration/UserDetails'} exact render={() => <UserDetails {...driverRegistrationProps} next="/Driver/Registration/AddressDetails"/>} />
      <Route path={'/Driver/Registration/AddressDetails'} exact render={() => <AddressDetails {...driverRegistrationProps} next="/Driver/Registration/BankAccountDetails"/>} />
      <Route path={'/Driver/Registration/AddressLookup'} exact render={() => <AddressLookup {...driverRegistrationProps}/>} />
      <Route path={'/Driver/Registration/BankAccountDetails'} exact render={() => <BankAccountDetails {...driverRegistrationProps}/>} />
      <Route path={'/Driver/Registration/DriverAccountType'} exact render={() => <DriverAccountType {...driverRegistrationProps}/>} />
      <Redirect to={'/Driver/Registration/DriverAccountType'}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    ...nextOwnProps
  };
};

export default withRouter(withExternalState(mapStateToProps)(DriverRegistration));

