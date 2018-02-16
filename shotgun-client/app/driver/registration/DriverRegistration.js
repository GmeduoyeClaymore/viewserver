import React, {Component} from 'react';
import {connect} from 'custom-redux';
import UserDetails from 'common/registration/UserDetails';
import DriverCapabilityDetails from './DriverCapabilityDetails';
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

class DriverRegistration extends Component {
  constructor() {
    super();
    this.state = INITIAL_STATE;
  }

  componentWillMount(){
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    registerNakedDao(dispatch, new DriverDao(client));
    register(dispatch, new ContentTypeDao(client, {userId: undefined}));
    register(dispatch, new ProductCategoryDao(client), {});
  }

  render() {
    return <Switch>
      <Route path={'/Driver/Registration/DriverRegistrationLanding'} exact render={() => <DriverRegistrationLanding {...this.props} context={this}/>} />
      <Route path={'/Driver/Registration/Login'} exact render={() => <DriverLogin {...this.props} context={this}/>} />
      <Route path={'/Driver/Registration/UserDetails'} exact render={() => <UserDetails {...this.props} context={this} next="/Driver/Registration/AddressDetails"/>} />
      <Route path={'/Driver/Registration/AddressDetails'} exact render={() => <AddressDetails {...this.props} context={this} next="/Driver/Registration/BankAccountDetails"/>} />
      <Route path={'/Driver/Registration/AddressLookup'} exact render={() => <AddressLookup {...this.props}/>} />
      <Route path={'/Driver/Registration/BankAccountDetails'} exact render={() => <BankAccountDetails {...this.props} context={this}/>} />
      <Route path={'/Driver/Registration/DriverAccountType'} exact render={() => <DriverAccountType {...this.props} context={this}/>} />
      <Redirect to={'/Driver/Registration/DriverAccountType'}/>
    </Switch>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverRegistration);

