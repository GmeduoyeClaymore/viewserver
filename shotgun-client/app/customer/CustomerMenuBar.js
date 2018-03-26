import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const CustomerMenuBar = ({history, match, isReady}) => {
  return <Footer>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push(`${match.path}/Checkout`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push(`${match.path}/CustomerOrders`)}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push(`${match.path}/UserRelationships`)}><Icon name='two-people'/></Button></FooterTab>
    <FooterTab><Button disabled={!isReady} transparent onPress={() => history.push(`${match.path}/Settings/CustomerSettings`)}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps)(CustomerMenuBar);

