import React, {Component} from 'react';
import {connect} from 'react-redux';
import DriverMenuBar from './DriverMenuBar';
import DriverOrders from './DriverOrders';
import DriverSettings from './DriverSettings';
import {driverServicesRegistrationAction } from 'driver/actions/DriverActions';
import {isAnyLoading} from 'common/dao';
import {Route, Redirect, Switch} from 'react-router-native';
import {Container} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';

class DriverLanding extends Component {
  static INITIAL_ROOT_NAME = 'DriverOrders';

  constructor(props) {
    super(props);
  }

  componentWillMount(){
    const {dispatch, client, userId} = this.props; 
    dispatch(driverServicesRegistrationAction(client,userId)); 
  }

  render() {
    const {match, busy} = this.props;

    return busy ? <LoadingScreen text="Loading Driver Landing Screen"/> :
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


