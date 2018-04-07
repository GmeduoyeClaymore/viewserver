import React, {Component} from 'react';
import {connect, Redirect} from 'custom-redux';
import {commonServicesRegistrationAction, logOut, unregisterAllDaos} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState, getLoadingError} from 'common/dao';
import {View, Button, Text} from 'native-base';
import {LoadingScreen} from 'common/components';
import PermissionsService from 'common/services/PermissionsService';


class LandingCommon extends Component {
  constructor(props) {
    super(props);
  }

  beforeNavigateTo() {
    const {dispatch, client} = this.props;
    dispatch(commonServicesRegistrationAction(client));
    PermissionsService.requestLocationPermission();
  }

  render() {
    const {busy, user, history, dispatch, errors} = this.props;

    const signOut = async () => {
      dispatch(logOut(() => history.push('/')));
      dispatch(unregisterAllDaos());
    };

    if (busy){
      return <LoadingScreen text="Logging You In"/>;
    }
    if (errors){
      return <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}><Text>Unable to find your user details. Please sign out and log back in {errors}</Text>
        <Button fullWidth paddedBottom signOutButton onPress={() => signOut(history)}><Text uppercase={false}>Sign out</Text></Button>
      </View>;
    }

    if (!user){
      return <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
        <LoadingScreen text="Waiting for user .. If this takes a while try logging out then in again"/>
        <Button fullWidth paddedBottom signOutButton onPress={() => signOut(history)}><Text uppercase={false}>Sign out</Text></Button>
      </View>;
    }
    switch (user.type){
    case 'driver':
      return <Redirect to="/Driver/Landing" just={true} history={history}/>;
    case 'customer':
      return <Redirect to="/Customer/Landing" just={true} history={history}/>;
    default:
      throw new Error(`Could not process user of type ${user.type} user is ${JSON.stringify(user)}`);
    }
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['userDao']),
  user: getDaoState(state, ['user'], 'userDao'),
  errors: getLoadingError(state, 'userDao'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(LandingCommon);

