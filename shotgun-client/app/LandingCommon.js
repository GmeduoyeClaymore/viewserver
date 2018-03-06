import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState} from 'common/dao';
import {Redirect} from 'react-router-native';
import {View, Button, Text} from 'native-base';
import {LoadingScreen} from 'common/components';
import PermissionsService from 'common/services/PermissionsService';
import PrincipalService from './common/services/PrincipalService';
const signOut = async (history) => {
  await PrincipalService.removeUserIdFromDevice();
  history.push('/Root');
};
class LandingCommon extends Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const {dispatch, client} = this.props;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client));
    PermissionsService.requestLocationPermission();
  }

  render() {
    const {busy, user, history} = this.props;

    if (busy){
      return <LoadingScreen text="Logging You In"/>;
    }
    if (!user){
      return <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}><Text>Unable to find your user details. Please sign out and log back in</Text>
        <Button fullWidth paddedBottom signOutButton onPress={() => signOut(history)}><Text uppercase={false}>Sign out</Text></Button>
      </View>;
    }
    switch (user.type){
    case 'driver':
      return <Redirect to="/Driver/Landing"/>;
    case 'customer':
      return <Redirect to="/Customer/Landing"/>;
    default:
      throw new Error(`Could not process user of type ${user.type} user is ${JSON.stringify(user)}`);
    }
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['userDao']),
  user: getDaoState(state, ['user'], 'userDao'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(LandingCommon);

