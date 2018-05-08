import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Image} from 'react-native';
import {AppImages} from 'common/assets/img/Images';

const CustomerMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Checkout`})}><Image source={AppImages.list} style={styles.image}/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/CustomerMyOrders`})}><Image source={AppImages.jobs} style={styles.image}/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/UserRelationships`})}><Image source={AppImages.onePerson} style={styles.image}/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Settings`})}><Image source={AppImages.cog} style={styles.image}/></Button></FooterTab>
  </Footer>;
};

const styles = {
  image: {
    resizeMode: 'contain',
    height: 30
  }
};


const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps, true, false)(CustomerMenuBar);

