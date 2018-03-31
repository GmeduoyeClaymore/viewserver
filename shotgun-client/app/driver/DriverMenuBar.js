import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const DriverMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${path}/Checkout`, transition: 'bottom'})}><Icon name='star'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${path}/DriverOrderRequests`, transition: 'bottom'})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${path}/DriverOrders`, transition: 'bottom'})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${path}/UserRelationships`, transition: 'bottom'})}><Icon style={{marginLeft: 10, marginRight: 10}} name='two-people'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${path}/Settings`, transition: 'bottom'})}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

export default connect(undefined, true, false)(DriverMenuBar);

