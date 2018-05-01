import React from 'react';
import {Text, Button, Container, Grid, Col, Row} from 'native-base';
import {Image} from 'react-native';
import {Icon} from 'common/components';
import {AppImages} from 'common/assets/img/Images';

export default DriverRegistrationLanding = ({history}) => {
  return <Container padded>
    <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
      <Icon name='back-arrow'/>
    </Button>
    <Grid>
      <Row size={50}>
        <Col>
          <Image source={AppImages.launch} style={styles.image}/>
        </Col>
      </Row>
      <Row size={50}>
        <Col>
          <Button fullWidth style={styles.signInButton} onPress={() => history.push('/Driver/Registration/Login')}><Text uppercase={false}>Sign in</Text></Button>
          <Button fullWidth light onPress={() => history.push('/Driver/Registration/UserDetails')}><Text uppercase={false}>I'm new, register</Text></Button>
        </Col>
      </Row>
    </Grid>
  </Container>;
};

const styles = {
  signInButton: {
    marginBottom: 15
  },
  image: {
    resizeMode: 'contain',
    width: '100%'
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  }
};
