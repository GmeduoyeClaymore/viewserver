import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const CustomerMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Checkout`})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/CustomerMyOrders`})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/UserRelationships`})}><Icon style={{marginLeft: 10, marginRight: 10}} name='one-person'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Settings`})}><Icon name='cog'/></Button></FooterTab>
  </Footer>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps, true, false)(CustomerMenuBar);

