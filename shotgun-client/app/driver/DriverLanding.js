import React, {Component} from 'react';
import {connect} from 'react-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverSettings from './DriverSettings';
import {driverServicesRegistrationAction } from 'driver/actions/DriverActions';
import {isAnyLoading} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Text, Content, Container} from 'native-base';

class DriverLanding extends Component {
  static INITIAL_ROOT_NAME = 'DriverOrders';

  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, client} = this.props;
    dispatch(driverServicesRegistrationAction(client));
  }

  render() {
    const {match, busy} = this.props;

    return busy ? <Content><Text>Loading Driver Landing.....</Text></Content> :
      <Container>
        <Switch>
          <Route path={`${match.path}/DriverOrders`} exact component={DriverOrders}/>
          <Route path={`${match.path}/DriverSettings`} exact component={DriverSettings} />}/>
          <Redirect to={`${match.path}/${DriverLanding.INITIAL_ROOT_NAME}`}/>
        </Switch>
        <DriverMenuBar/>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['driverDao']),
  ...nextOwnProps
});

export default connect(mapStateToProps)(DriverLanding);


