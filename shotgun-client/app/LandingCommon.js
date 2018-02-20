import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState} from 'common/dao';
import {Redirect} from 'react-router-native';
import {View, Button, Text} from 'native-base';
import Logger from 'common/Logger';
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
    const {dispatch, userId, client} = this.props;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client, userId));
    PermissionsService.requestLocationPermission();
  }

  render() {
    const {busy, user, history} = this.props;

    if (busy){
      return <LoadingScreen text="Logging You In"/>;
    }
    if (!user){
      return <View style={{flex: 1}}><LoadingScreen text="Cannot find user on server clear storage then try again"/>
        <Button fullWidth paddedBottom signOutButton onPress={() => signOut(history)}><Text uppercase={false}>Sign out</Text></Button>
      </View>;
    }
    switch (user.type){
    case 'driver':
      return <Redirect to="/Driver/Landing"/>;
    case 'customer':
      return <Redirect to="/Customer/Landing"/>;
    default:
      Logger.error(`Could not process user of type ${user.type} user is ${JSON.stringify(user)}`);
      //TODO -return an error screen here
      return null;
    }
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['userDao']),
  user: getDaoState(state, ['user'], 'userDao'),
  ...nextOwnProps
});

export default connect(mapStateToProps)(LandingCommon);

