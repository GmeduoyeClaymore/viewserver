import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import PagingListView from 'common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Spinner, Header, Body, Title, Tab, List} from 'native-base';
import {getDaoState} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import OrderRequest from 'common/components/OrderRequest';
import Tabs from 'common/components/Tabs';

class DriverOrders extends Component{
  constructor(props){
    super(props);
    this.state = {isCompleted: false};
  }

  setIsCompleted(isCompleted){
    if (this.state.isCompleted !== isCompleted) {
      this.setState({isCompleted});
    }
  }

  render(){
    const {isCompleted} = this.state;

    const reportOptions = {
      isCompleted,
      columnsToSort: [{ name: 'eta', direction: 'asc' }],
      reportId: 'driverOrderSummary'};

    const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
    const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No jobs assigned</Text></View>;
    const RowView = (orderSummary) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next='/Driver/DriverOrderDetail'/>;

    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={isCompleted ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => this.setIsCompleted(i == 1)}>
        <Tab heading="Live Jobs"/>
        <Tab heading="Complete"/>
      </Tabs>
      <Content>
        <List>
          <PagingListView
            daoName='orderSummaryDao'
            dataPath={['orders']}
            rowView={RowView}
            options={reportOptions}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            pageSize={10}
            headerView={() => null}
          />
        </List>
      </Content>
    </Container>;
  }
}

DriverOrders.PropTypes = {
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  vehicle: getDaoState(state, ['vehicle'], 'vehicleDao'),
  position: getDaoState(state, ['position'], 'userDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DriverOrders));
