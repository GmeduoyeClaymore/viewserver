import React, {Component} from 'react';
import {withExternalState, Route, ReduxRouter} from 'custom-redux';
import UserDetails from 'common/registration/UserDetails';
import PartnerAccountType from './PartnerAccountType';
import PartnerLogin from './PartnerLogin';
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
import {nextAction} from './PartnerRegistrationUtils';
import {isAnyOperationPending, getOperationError} from 'common/dao';

class PartnerRegistration extends Component {
  static InitialState = INITIAL_STATE;
  static stateKey = 'partnerRegistration';
  constructor() {
    super();
  }

  beforeNavigateTo(){
    this.setState(PartnerRegistration.InitialState);
  }

  componentDidMount(){
    const {dispatch, client} = this.props;
    registerNakedDao(dispatch, new PartnerDao(client));
    register(dispatch, new ContentTypeDao(client));
    register(dispatch, new NotificationsDao(client));
    register(dispatch, new ProductCategoryDao(client));
    register(dispatch, new UserRelationshipDao(client));
    register(dispatch, new UserRelationshipDao(client, 'singleUserRelationshipDao'));
    register(dispatch, new ProductDao(client), {});
  }

  render() {
    const partnerRegistrationProps = {...this.props, nextAction, submitButtonCaption: 'Register'};
    const {path} = this.props;
    const routeProps = {transition: 'left', stateKey: PartnerRegistration.stateKey};
    return <ReduxRouter  name="PartnerRegistrationRouter" resizeForKeyboard={false} {...partnerRegistrationProps} defaultRoute={'PartnerRegistrationLanding'}>
      <Route {...routeProps} path={'PartnerRegistrationLanding'} exact component={PartnerRegistrationLanding}/>
      <Route {...routeProps} path={'Login'} exact component={PartnerLogin}/>
      <Route {...routeProps} path={'UserDetails'} next={`${path}/PartnerAccountType`} exact component={UserDetails}/>
      <Route {...routeProps} path={'AddressDetails'} next={`${path}/PartnerAccountType`} exact component={AddressDetails}/>
      <Route {...routeProps} path={'AddressLookup'} exact component={AddressLookup} showRecent={false}/>
      <Route {...routeProps} path={'PartnerAccountType'} exact component={PartnerAccountType}/>
    </ReduxRouter>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  const errors = getOperationError(state, 'loginDao', 'registerAndLoginPartner') || '';
  const busy = isAnyOperationPending(state, [{ loginDao: 'registerAndLoginPartner'}]);
  return {
    parentMatch,
    ...nextOwnProps,
    busy,
    errors
  };
};

export default withExternalState(mapStateToProps)(PartnerRegistration);

