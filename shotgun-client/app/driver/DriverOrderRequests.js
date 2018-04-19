import React, {Component} from 'react';
import {connect, ReduxRouter, Route, memoize} from 'custom-redux';
import {LoadingScreen, Tabs } from 'common/components';
import {Container, Header, Body, Title, Tab } from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import DriverOrderRequestItems from './DriverOrderRequestItems';

class DriverOrderRequests extends Component{
  goToTabNamed = (name) => {
    const {history, path} = this.props;
    history.replace(`${path}/ContentTypeId${name}X`);
  }

  render(){
    const { busy, selectedContentTypes = [], height, history, navContainerOverride, parentPath, path, contentTypeOptions, selectedContentTypeIndex} = this.props;

    return busy ? <LoadingScreen text="Loading available jobs..." /> : <Container>
      <Header hasTabs>
        <Body><Title>Available Jobs</Title></Body>
      </Header>
      <Tabs initialPage={selectedContentTypeIndex} page={selectedContentTypeIndex} {...shotgun.tabsStyle}>
        {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} onPress={() => this.goToTabNamed(c.contentTypeId)}/>)}
      </Tabs>
      {selectedContentTypes[0] ? <ReduxRouter  name="DriverOrderRequestRouter" height={height - shotgun.tabHeight} defaultRoute={`ContentTypeId${selectedContentTypes[0].contentTypeId}X`} {...{busy, selectedContentTypes, navContainerOverride, history, path, parentPath, contentTypeOptions}}>
        {selectedContentTypes.map(c => <Route key={c.contentTypeId} parentPath={parentPath} path={`ContentTypeId${c.contentTypeId}X`} contentType={c} component={DriverOrderRequestItems} />)}
      </ReduxRouter> : null}
    </Container>;
  }
}

//TODO - tidy up this unholy mess :)
const getSelectedContentTypeFromLocation = memoize((history, selectedContentTypes) => {
  const {location} = history;
  if (!selectedContentTypes || !location.pathname.includes('/ContentTypeId')){
    return 0;
  }
  return selectedContentTypes.find(element => { return location.pathname.includes(`/ContentTypeId${element.contentTypeId}X`);});
});

const getSelectedContentTypesFromUser = memoize((user, availableContentTypes) => {
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]).map( key => parseInt(key, 10)) : [];
  return {
    selectedContentTypes: availableContentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId)),
    selectedContentTypeOptions
  };
});

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  if (!user){
    return;
  }
  //TODO - urg this could also do with some love
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao') || [];
  const {history} = initialProps;
  const {selectedContentTypes, selectedContentTypeOptions} = getSelectedContentTypesFromUser(user, contentTypes);
  const selectedContentType = getSelectedContentTypeFromLocation(history, selectedContentTypes) || selectedContentTypes[0] || {};
  const {contentTypeId} = selectedContentType;
  const contentTypeOptions = contentTypeId ? selectedContentTypeOptions[contentTypeId] : {};
  let selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);
  selectedContentTypeIndex = !!~selectedContentTypeIndex ? selectedContentTypeIndex : 0;

  return {
    ...initialProps,
    selectedContentTypes,
    contentTypeOptions,
    selectedContentTypeIndex,
    contentTypeId,
    selectedContentType,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !selectedContentType,
    user
  };
};

export default connect(mapStateToProps)(DriverOrderRequests);
