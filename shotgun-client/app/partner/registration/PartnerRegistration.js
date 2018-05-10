import React, {Component} from 'react';
import {withExternalState, Route, ReduxRouter} from 'custom-redux';
import UserDetails from 'common/registration/UserDetails';
import PartnerAccountType from './PartnerAccountType';
import PartnerLogin from './PartnerLogin';
import BankAccountDetails from './BankAccountDetails';
import PartnerRegistrationLanding from './PartnerRegistrationLanding';
import AddressDetails from 'common/registration/AddressDetails';
import AddressLookup from 'common/components/maps/AddressLookup';
import {registerNakedDao, register} from 'common/actions/CommonActions';
import PartnerDao from 'partner/dao/PartnerDao';
import ContentTypeDao from 'common/dao/ContentTypeDao';
import NotificationsDao from 'common/dao/NotificationsDao';
import {INITIAL_STATE} from './PartnerRegistrationInitialState';
import ProductCategoryDao from 'common/dao/ProductCategoryDao';
import UserRelationshipDao from 'common/dao/UserRelationshipDao';
import ProductDao from 'common/dao/ProductDao';

class PartnerRegistration extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'partnerRegistration';
  constructor() {
    super();
  }

  componentDidMount(){
    const {dispatch, client} = this.props;
    registerNakedDao(dispatch, new PartnerDao(client));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new NotificationsDao(client));
    register(dispatch, new ProductCategoryDao(client));
    register(dispatch, new UserRelationshipDao(client));
    register(dispatch, new ProductDao(client), {});
  }

  render() {
    const partnerRegistrationProps = {...this.props, stateKey: PartnerRegistration.stateKey};
    const {path} = this.props;
    return <ReduxRouter  name="PartnerRegistrationRouter" resizeForKeyboard={true} {...partnerRegistrationProps} defaultRoute={'PartnerRegistrationLanding'}>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'PartnerRegistrationLanding'} exact component={PartnerRegistrationLanding}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'Login'} exact component={PartnerLogin}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'UserDetails'} next={`${path}/AddressDetails`} exact component={UserDetails}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'AddressDetails'} next={`${path}/BankAccountDetails`} exact component={AddressDetails}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'AddressLookup'} exact component={AddressLookup}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'BankAccountDetails'} exact component={BankAccountDetails}/>
      <Route stateKey={PartnerRegistration.stateKey} transition='left' path={'PartnerAccountType'} exact component={PartnerAccountType}/>
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

export default withExternalState(mapStateToProps)(PartnerRegistration);

