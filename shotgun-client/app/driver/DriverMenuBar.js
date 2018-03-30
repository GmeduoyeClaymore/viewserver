import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const DriverMenuBar = ({history, match}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${match.path}/Checkout`, transition: 'bottom'})}><Icon name='star'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${match.path}/DriverOrderRequests`, transition: 'bottom'})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${match.path}/DriverOrders`, transition: 'bottom'})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${match.path}/UserRelationships`, transition: 'bottom'})}><Icon style={{marginLeft: 10, marginRight: 10}} name='two-people'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push({pathname: `${match.path}/Settings`, transition: 'bottom'})}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

export default connect(undefined, true, false)(DriverMenuBar);

