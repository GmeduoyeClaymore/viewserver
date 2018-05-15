import React from 'react';
import {Text, Button, Content, Grid, Col, Row, View, H1} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon} from 'common/components';

export default RegistrationCommon = ({history}) => {
  return <Content padded contentContainerStyle={styles.container}>
    <View style={styles.titleView}>
      <H1 style={styles.h1}>Welcome to Shotgun</H1>
      <Text subTitle>Create and work on jobs for the building, waste and delivery trades</Text>
    </View>
    <View style={styles.productSelectView}>
      <Button style={[styles.productSelectButton, {marginBottom: shotgun.contentPadding}]} light onPress={() => history.push('/Customer/Registration')}>
        <Text style={styles.productSelectText} uppercase={false}>Looking for people?</Text>
      </Button>
      <Button style={styles.productSelectButton} light onPress={() => history.push('/Partner/Registration')}>
        <Text style={styles.productSelectText} uppercase={false}>Looking for work?</Text>
      </Button>
    </View>
  </Content>;
};

const styles = {
  h1: {
    width: '90%',
    marginBottom: 30
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    backgroundColor: shotgun.brandPrimary
  },
  titleView: {
    flex: 1,
    justifyContent: 'flex-end'
  },
  productSelectView: {
    flex: 1,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  productSelectButton: {
    width: '100%',
    height: 80,
    justifyContent: 'center'
  },
  productSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center'
  }
};
