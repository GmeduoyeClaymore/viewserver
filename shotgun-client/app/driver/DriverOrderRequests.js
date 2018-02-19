import React, {Component} from 'react';
import { connect } from 'custom-redux';
import { PagingListView, LoadingScreen, OrderRequest, Tabs } from 'common/components';
import { withRouter } from 'react-router';
import { View, Text, Container, Spinner, Header, Body, Title, Tab } from 'native-base';
import { getDaoState, isAnyLoading, getNavigationProps } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const Paging = () => <Spinner />;
const NoItems = () => <Text empty>No jobs available</Text>;
const RowView = ({ item: orderSummary, isLast, isFirst }) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderRequestDetail' />;

class DriverOrderRequests extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.state  = {};
  }


  onChangeTab(index){
    const {selectedContentType, selectedContentTypes, history} = this.props;
    const newSelectedContentType = selectedContentTypes[index];
    if (this.pagingListView){
      this.pagingListView.wrappedInstance.reset();
    }
    if (selectedContentType.contentTypeId !== newSelectedContentType.contentTypeId) {
      history.replace('/Driver/DriverOrderRequests', { contentTypeId: newSelectedContentType.contentTypeId });
    }
  }

  render(){
    const { selectedContentTypeIndex, position, busy, selectedContentTypes, contentTypeOptions, selectedContentType } = this.props;
    if (busy) {
      return <LoadingScreen text="Loading Map" />;
    }
  
    const {onChangeTab} = this;
  
    return <Container>
      <Header hasTabs>
        <Body><Title>Available Jobs</Title></Body>
      </Header>
      <Tabs initialPage={selectedContentTypeIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} />)}
      </Tabs>
      <View style={{ flex: 1 }}>
        <PagingListView
          ref={c=> {this.pagingListView = c;}}
          daoName='orderRequestDao'
          dataPath={['driver', 'orders']}
          rowView={RowView}
          options={{contentType: selectedContentType, contentTypeOptions, position}}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          pageSize={10}
          headerView={() => null}
        />
      </View>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const vehicle = getDaoState(state, ['vehicle'], 'vehicleDao');
  const position = getDaoState(state, ['position'], 'userDao');
  const user = getDaoState(state, ['user'], 'userDao');
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const navigationProps = getNavigationProps(initialProps) || [];
 
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]).map( key => parseInt(key, 10)) : [];
  const selectedContentTypes = contentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId));
  let { contentTypeId } = navigationProps;
  contentTypeId = contentTypeId || (selectedContentTypes[0] && selectedContentTypes[0].contentTypeId);
  const selectedContentType = contentTypes.find(ct => ct.contentTypeId === contentTypeId) || selectedContentTypes[0];
  const selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);
  const contentTypeOptions = contentTypeId ? selectedContentTypeOptions[contentTypeId] : {};

  return {
    ...initialProps,
    selectedContentTypes,
    contentTypeOptions,
    contentTypeId,
    selectedContentType,
    selectedContentTypeIndex,
    isDelivery: navigationProps.isDelivery !== undefined ? navigationProps.isDelivery : true,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !vehicle || !position,
    vehicle,
    user,
    position
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequests));
