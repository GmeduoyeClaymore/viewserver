import React from 'react';
import {Text, Button, Content, Grid, Col, Row, View, H1} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {Icon} from 'common/components';

export default RegistrationCommon = ({history}) => {
  return <Content padded contentContainerStyle={styles.container}>
    <View style={styles.titleView}>
      <H1 style={styles.h1}>Welcome to Shotgun</H1>
      <Text subTitle>What are you here to do?</Text>
    </View>
    <View style={styles.productSelectView}>
      <Grid>
        <Row>
          <Col size={45}>
            <Row size={35}><Button large onPress={() => history.push('/Customer/Registration')}><Icon name='schedule-delivery'/></Button></Row>
            <Row size={65} style={styles.productSelectTextRow}><Text style={styles.productSelectText}>I need to something ..</Text></Row>
          </Col>
          <Col size={10}>
            <Row style={styles.orRow}><Text>or</Text></Row>
          </Col>
          <Col size={45}>
            <Row size={35}><Button large onPress={() => history.push('/Driver/Registration')}><Icon name='drive'/></Button></Row>
            <Row size={65} style={styles.productSelectTextRow}><Text style={styles.productSelectText}>I have something to supply</Text></Row>
          </Col>
        </Row>
      </Grid>
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
  orRow: {
    justifyContent: 'center',
    alignItems: 'flex-start',
    paddingTop: 30
  },
  productSelectTextRow: {
    justifyContent: 'center',
    alignItems: 'flex-start'
  },
  productSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    width: '80%',
    textAlign: 'center'
  }
};
