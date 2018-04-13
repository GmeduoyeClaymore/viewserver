import React, {Component} from 'react';
import {Grid, Tab} from 'native-base';
import {Tabs} from 'common/components';
import {ReduxRouter, Route} from 'custom-redux';
import CallButtons from './CallButtons';
import OrderPhotoUpload from './OrderPhotoUpload';
import MapDetails from './MapDetails';

export default class PersonellCustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  goToTabNamed(name){
    const {history, path} = this.props;
    history.replace({pathname: `${path}/${name}`});
  }
  
  render() {
    const {history} = this.props;
    const {goToTabNamed} = this;
    const page = history.location.pathname.endsWith('Directions')  ? 1 : 0;
    return <Grid>
      <DriverDetails {...this.props}/>
      <CallButtons {...this.props}/>
      <Tabs initialPage={page} page={page}  {...shotgun.tabsStyle}>
        <Tab heading="Photos" onPress={() => goToTabNamed('OrderPhotoUpload')}/>
        <Tab heading="Directions" onPress={() => goToTabNamed('MapDetails')}/>
      </Tabs>
      <ReduxRouter defaultRoute="OrderPhotoUpload">
        <Route key="OrderPhotoUpload" path="OrderPhotoUpload" component={OrderPhotoUpload}/>
        <Route key="MapDetails" path="MapDetails" component={MapDetails}/>
      </ReduxRouter>
    </Grid>;
  }
}
  
