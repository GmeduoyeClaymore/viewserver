import React from 'react';
import {Button, Text, Content, List, ListItem, Header, Container, Body, Title, Col, Row, View} from 'native-base';
import {connect} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {Image, Linking} from 'react-native';
import {AverageRating} from 'common/components';
import {logOut, unregisterAllDaosAndResetComponentState} from 'common/actions/CommonActions';
import {AppImages} from 'common/assets/img/Images';
//import DeviceInfo from 'react-native-device-info';

const feedbackSubject = '';//`Partner Feedback from ${DeviceInfo.getApplicationName()} version ${DeviceInfo.getReadableVersion()} running on ${DeviceInfo.getModel()}${DeviceInfo.isEmulator() ? ' emulator' : ''} ${DeviceInfo.getSystemName()} ${DeviceInfo.getSystemVersion()}`;

const PartnerSettings = ({history, dispatch, user, parentPath}) => {
  const signOut = async () => {
    dispatch(logOut(() => history.push('/')));
    dispatch(unregisterAllDaosAndResetComponentState());
  };


  return user ? <Container>
    <Header>
      <Body style={{width: '100%'}}>
        <Row style={{width: '100%'}}>
          <Col size={200}>
            <Title>{user.firstName} {user.lastName}</Title>
          </Col>
          <Col  size={30}>
            <View style={{flexDirection: 'column', justifyContent: 'center'}}><AverageRating rating={user.ratingAvg}/></View>
          </Col>
          <Col  size={30}>
            {user.imageUrl != undefined ? <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.image}/> : null}
          </Col>
        </Row>
      </Body>
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateUserDetails`)}>
          <Text style={styles.text}>Personal details</Text>
          <Image source={AppImages.onePerson} style={styles.icon}/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/PartnerMyOrders`)}>
          <Text style={styles.text}>Completed and Rejected Jobs</Text>
          <Image source={AppImages.jobs} style={styles.icon}/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdatePaymentCardDetails`)}>
          <Text style={styles.text}>Payment cards</Text>
          <Image source={AppImages.payment} style={styles.icon}/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateBankAccountDetails`)}>
          <Text style={styles.text}>Bank Details</Text>
          <Image source={AppImages.bank} style={styles.icon}/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/ConfigureServices`)}>
          <Text style={styles.text}>Configure Services</Text>
          <Image source={AppImages.cog} style={styles.icon}/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => Linking.openURL(`mailto:accounts@shotgun.ltd?subject=${feedbackSubject}`)}>
          <Text style={styles.text}>Give us feedback</Text>
          <Image source={AppImages.feedback} style={styles.icon}/>
        </ListItem>
      </List>
      <Button fullWidth paddedTopBottom signOutButton onPress={signOut}><Text uppercase={false}>Sign out</Text></Button>
    </Content>
  </Container> : null;
};

const styles = {
  text: {
    fontSize: 16
  },
  image: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 50,
    marginTop: 5
  },
  icon: {
    resizeMode: 'contain',
    height: 50,
    position: 'absolute',
    right: 10
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  user: getDaoState(state, ['user'], 'userDao'),
});

export default connect(
  mapStateToProps, true, false
)(PartnerSettings);


