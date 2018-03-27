import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const DriverMenuBar = ({history, match}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/Checkout`)}><Icon name='star'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/DriverOrderRequests`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/DriverOrders`)}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/UserRelationships`)}><Icon name='two-people'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/Settings`)}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

export default connect(undefined, true, false)(DriverMenuBar);

