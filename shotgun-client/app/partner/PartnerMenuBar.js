import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const PartnerMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Checkout`})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/PartnerAvailableOrders`})}><Icon name='available-jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/PartnerMyOrders`, transition: 'left'})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/UserRelationships`})}><Icon name='one-person'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Settings`})}><Icon name='cog'/></Button></FooterTab>
  </Footer>;
};

export default connect(undefined, true, false)(PartnerMenuBar);

