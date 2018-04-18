import React, {Component} from 'react';
import {connect, Redirect} from 'custom-redux';
import {commonServicesRegistrationAction, logOut, unregisterAllDaosAndResetComponentState} from 'common/actions/CommonActions';
import {isAnyLoading, getDaoState, getLoadingError} from 'common/dao';
import {Content, Button, Text} from 'native-base';
import {LoadingScreen} from 'common/components';

class LandingCommon extends Component {
  constructor(props) {
    super(props);
  }

  beforeNavigateTo() {
    const {dispatch, client} = this.props;
    dispatch(commonServicesRegistrationAction(client));
  }

  render() {
    const {busy, user, history, dispatch, errors} = this.props;

    const signOut = async () => {
      dispatch(logOut(() => history.push('/')));
      dispatch(unregisterAllDaosAndResetComponentState());
    };

    if (busy || !user){
      return <LoadingScreen text="Signing in"/>;
    }

    if (errors){
      return <Content contentContainerStyle={styles.contentStyle}>
        <Text>Unable to find your user details. Please sign out and log back in {errors}</Text>
        <Button fullWidth paddedBottom signOutButton onPress={() => signOut(history)}><Text uppercase={false}>Sign out</Text></Button>
      </Content>;
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

const styles = {
  contentStyle: {
    justifyContent: 'center',
    alignItems: 'center'
  }
};

export default connect(mapStateToProps)(LandingCommon);

