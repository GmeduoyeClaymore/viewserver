import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const OrderedRoutes = ['Checkout', 'CustomerMyOrders', 'UserRelationships', 'Settings'];

const DefaultRoute = 'CustomerMyOrders';

const CustomerMenuBar = ({history, path}) => {
  const getSelectedRouteIndex = () => {
    const selectedRouteIndex = OrderedRoutes.findIndex(th => history.location.pathname.includes(`${path}/${th}`));
    return  !!~selectedRouteIndex ? selectedRouteIndex : OrderedRoutes.indexOf(DefaultRoute);
  };
  const getTransition = (route) => {
    return getSelectedRouteIndex() > OrderedRoutes.indexOf(route) ? 'right' : 'left';
  };
  const isSelected = (route) => {
    return getSelectedRouteIndex() == OrderedRoutes.indexOf(route);
  };
  return <Footer>
    <FooterTab><Button transparent light={!isSelected('Checkout')}  onPress={() => history.just({pathname: `${path}/Checkout`, transition: getTransition('Checkout')})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent light={!isSelected('CustomerMyOrders')} onPress={() => history.just({pathname: `${path}/CustomerMyOrders`, transition: getTransition('CustomerMyOrders')})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent light={!isSelected('UserRelationships')} onPress={() => history.just({pathname: `${path}/UserRelationships`, transition: getTransition('UserRelationships')})}><Icon style={{marginLeft: 10, marginRight: 10}} name='one-person'/></Button></FooterTab>
    <FooterTab><Button transparent light={!isSelected('Settings')} onPress={() => history.just({pathname: `${path}/Settings`, transition: getTransition('Settings')})}><Icon name='cog'/></Button></FooterTab>
  </Footer>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps, true, false)(CustomerMenuBar);

