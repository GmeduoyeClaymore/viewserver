import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Image} from 'react-native';
import {Icon} from 'common/components';
import {AppImages} from 'common/assets/img/Images';

const PartnerMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Checkout/ContentTypeSelect`})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/PartnerOrderRequests`})}><Image source={AppImages.spanner} style={styles.image}/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/PartnerOrders`})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/UserRelationships`})}><Icon style={{marginLeft: 10, marginRight: 10}} name='two-people'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Settings`})}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

const styles = {
  image: {
    resizeMode: 'contain',
    height: '65%',
    width: '100%',
  }
}

export default connect(undefined, true, false)(PartnerMenuBar);

