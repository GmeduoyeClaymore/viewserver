import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const CustomerMenuBar = ({history, match, isReady}) => {
  return <Footer>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push({pathname: '/Customer/Checkout', transition: 'bottom'})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push({pathname: '/Customer/CustomerOrders', transition: 'bottom'})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push({pathname: '/Customer/UserRelationships', transition: 'bottom'})}><Icon style={{marginLeft: 10, marginRight: 10}} name='two-people'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push({pathname: '/Customer/Settings', transition: 'bottom'})}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps)(CustomerMenuBar);

