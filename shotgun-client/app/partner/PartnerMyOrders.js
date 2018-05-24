import React, {Component} from 'react';
import {Tabs, Icon} from 'common/components';
import {Container, Header, Body, Title, Tab, Left, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect, ReduxRouter, Route} from 'custom-redux';
import PartnerMyOrdersListView from './PartnerMyOrdersListView';
import OrderSummaryDao from 'common/dao/OrderSummaryDao';
import {getPartnerFriendlyOrderStatusName, getPartnerOrderColor, getCustomerOrderColor, getDeliveryFriendlyOrderStatusName, getRubbishFriendlyOrderStatusName, getProductBasedFriendlyOrderStatusName} from 'common/constants/OrderStatuses';
import * as ContentTypes from 'common/constants/ContentTypes';
import {NegotiationStatuses, getPartnerFriendlyNegotiationStatusName, getPartnerNegotiationColor} from 'common/constants/NegotiationStatuses';

class PartnerMyOrders extends Component{
  goToTabNamed = (name) => {
    const {history, isCompleted, path, canGoBack} = this.props;
    history.replace({pathname: `${path}/${name}`}, {isCompleted, canGoBack});
  }

  getRespondedAndDeclinedOptions = () => {
    const {isCompleted} = this.props;
    return {
      responseStatuses: isCompleted ? [NegotiationStatuses.REJECTED] : [NegotiationStatuses.DECLINED, NegotiationStatuses.RESPONDED]
    };
  }

  getAcceptedOptions = () => {
    const {isCompleted} = this.props;
    return {
      isCompleted,
      responseStatuses: [NegotiationStatuses.ACCEPTED]
    };
  }

  getOrdersWhereIAmTheCustomerOptions = () => {
    const {isCompleted = false} = this.props;
    return {...OrderSummaryDao.CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS, isCompleted, userId: '@userId'};
  }

  getHeading = (heading) => {
    if (heading === 'Responded' && this.props.isCompleted){
      return 'Rejected';
    }
    if (heading === 'Posted' && this.props.isCompleted){
      return 'Completed for Me';
    }
    if (heading === 'Accepted' && this.props.isCompleted){
      return 'Completed by Me';
    }
    return heading;
  }

  render() {
    const {history, selectedTabIndex, isCompleted, canGoBack, height, ...rest} = this.props;
    const {getAcceptedOptions, getRespondedAndDeclinedOptions, getOrdersWhereIAmTheCustomerOptions} = this;
    const routerHeight = height - shotgun.tabHeight;

    return <Container>
      <Header hasTabs withButton={canGoBack}>
        {canGoBack ? <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => history.goBack()} />
          </Button>
        </Left> : null }
        <Body><Title>{`My Jobs ${isCompleted ? ' (Completed)' : ''}`}</Title></Body>
      </Header>
      <Tabs initialPage={selectedTabIndex}  page={selectedTabIndex} {...shotgun.tabsStyle}>
        {TabHeadings.map(
          (heading, idx) => <Tab key={idx} tabsStyle={{textAlign: 'center'}} heading={this.getHeading(heading)} onPress={() => this.goToTabNamed(heading)}/>
        )}
      </Tabs>
      <ReduxRouter  name="PartnerOrdersRouter" height={routerHeight} defaultRoute='Responded' {...rest} >
        <Route path={'Responded'} orderStatusResolver={getPartnerFriendlyNegotiationStatusName} orderColorResolver={getPartnerNegotiationColor} emptyCaption={`You have no ${isCompleted ? 'completed' : ''} responded jobs`}  daoName='partnerOrderResponseDao' options={getRespondedAndDeclinedOptions()} isCustomer={false} component={PartnerMyOrdersListView}/>
        <Route path={'Accepted'} orderStatusResolver={getPartnerFriendlyOrderStatusName} orderColorResolver={getPartnerOrderColor} emptyCaption={`You have no ${isCompleted ? 'completed' : ''} accepted jobs`} daoName='partnerOrderResponseDao' options={getAcceptedOptions()} isCustomer={false} component={PartnerMyOrdersListView}/>
        <Route path={'Posted'} orderStatusResolver={getCustomerBasedOrderStatus} orderColorResolver={getCustomerOrderColor} emptyCaption={`You have no ${isCompleted ? 'completed' : ''} posted jobs`} daoName='orderSummaryDao' options={getOrdersWhereIAmTheCustomerOptions()} isCustomer={true} component={PartnerMyOrdersListView}/>
      </ReduxRouter>
    </Container>;
  }
}

const TabHeadings = [
  'Responded',
  'Accepted',
  'Posted',
];

const getSelectedTabIndex = (currentLocation, path) => {
  const result = TabHeadings.findIndex(th => currentLocation.includes(`${path}/${th}`));
  return !!~result ? result : 0;
};

const getCustomerBasedOrderStatus = order => {
  const resources = resourceDictionary.resolve(order.orderContentTypeId);
  return resources.OrderStatusResolver(order);
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary().
  property('OrderStatusResolver', getProductBasedFriendlyOrderStatusName).
    delivery(getDeliveryFriendlyOrderStatusName).
    rubbish(getRubbishFriendlyOrderStatusName);
    /*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  const {history, path} = initialProps;
  const selectedTabIndex = getSelectedTabIndex(history.location.pathname, path);
  return {
    selectedTabIndex,
    ...initialProps
  };
};

export default connect(mapStateToProps )(PartnerMyOrders);
