import React from 'react';
import {Text, Button, Container, View, H1} from 'native-base';
import {Icon} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';

export default PartnerRegistrationLanding = ({history}) => {
  return <Container padded contentContainerStyle={styles.container}>
    <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
      <Icon name='back-arrow'/>
    </Button>
    <View style={styles.titleView}>
      <H1 style={styles.h1}>Welcome to Shotgun</H1>
      <Text subTitle>Create and work on jobs for the building, waste and delivery trades</Text>
    </View>
    <View style={styles.productSelectView}>
      <Button fullWidth style={styles.signInButton} onPress={() => history.push('/Partner/Registration/Login')}><Text uppercase={false}>Sign in</Text></Button>
      <Button fullWidth light onPress={() => history.push('/Partner/Registration/UserDetails')}><Text uppercase={false}>I'm new, register</Text></Button>
    </View>
  </Container>;
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
