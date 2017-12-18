import React, {Component} from 'react';
import {connect} from 'react-redux';
import {unregisterAllDaos, commonServicesRegistrationAction} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState} from 'common/dao';
import {Redirect} from 'react-router-native';
import Logger from 'common/Logger';
import LoadingScreen from 'common/components/LoadingScreen';
import PermissionsService from 'common/services/PermissionsService';

class LandingCommon extends Component {
  constructor(props) {
    super(props);
  }

  componentWillMount() {
    const {dispatch, userId, client} = this.props;
    dispatch(unregisterAllDaos());
    dispatch(commonServicesRegistrationAction(client, userId));
  }

  componentDidMount(){
    PermissionsService.requestLocationPermission();
  }

  render() {
    const {busy, user} = this.props;

    if (busy){
      return <LoadingScreen text="Logging You In"/>;
    }
    if (!user){
      return <LoadingScreen text="Cannot find user on server clear storage then try again"/>;
    }
    switch (user.type){
    case 'driver':
      return <Redirect to="/Driver/Landing"/>;
    case 'customer':
      return <Redirect to="/Customer/Landing"/>;
    default:
      Logger.error(`Could not process user of type ${user.type}`);
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

