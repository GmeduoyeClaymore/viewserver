import React, {Component} from 'react';
import {Grid, Tab} from 'native-base';
import {Tabs} from 'common/components';
import {ReduxRouter, Route} from 'custom-redux';
import CallButtons from './CallButtons';
import OrderPhotoUpload from './OrderPhotoUpload';
import MapDetails from './MapDetails';
import PartnerDetails from 'common/components/UserInfo';
import shotgun from 'native-base-theme/variables/shotgun';
export default class PersonellCustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }

  goToTabNamed(name){
    const {history, path} = this.props;
    history.replace({pathname: `${path}/${name}`});
  }
  
  render() {
    const {history, path} = this.props;
    const {goToTabNamed} = this;
    const page = history.location.pathname.endsWith('Directions')  ? 1 : 0;
    return <Grid>
      <Tabs initialPage={page} page={page}  {...shotgun.tabsStyle}>
        <Tab heading="Photos" onPress={() => goToTabNamed('OrderPhotoUpload')}/>
        <Tab heading="Directions" onPress={() => goToTabNamed('MapDetails')}/>
      </Tabs>
      <ReduxRouter {...this.props} path={path} defaultRoute="OrderPhotoUpload">
        <Route key="OrderPhotoUpload" path="OrderPhotoUpload" component={OrderPhotoUpload}/>
        <Route key="MapDetails" path="MapDetails" component={MapDetails}/>
      </ReduxRouter>
    </Grid>;
  }
}
  
