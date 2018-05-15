import React from 'react';
import {Button, Text, Content, List, ListItem, Header, Container, Body, Title, Col, Row, View} from 'native-base';
import {connect} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {Image, Linking} from 'react-native';
import {Icon, AverageRating} from 'common/components';
import {logOut, unregisterAllDaosAndResetComponentState} from 'common/actions/CommonActions';
//import UserStatusControl from 'common/components/relationships/UserStatusControl';
//import DeviceInfo from 'react-native-device-info';

const feedbackSubject = '';//`Partner Feedback from ${DeviceInfo.getApplicationName()} version ${DeviceInfo.getReadableVersion()} running on ${DeviceInfo.getModel()}${DeviceInfo.isEmulator() ? ' emulator' : ''} ${DeviceInfo.getSystemName()} ${DeviceInfo.getSystemVersion()}`;

const PartnerSettings = ({history, dispatch, user, parentPath}) => {
  const signOut = async () => {
    dispatch(logOut(() => history.push('/')));
    dispatch(unregisterAllDaosAndResetComponentState());
  };


  return user ? <Container>
    <Header>
      <Body>
        <Row>
          <Col size={70}>
            <Title numberOfLines={1} style={{alignSelf: 'flex-start'}}>{user.firstName} {user.lastName}</Title>
          </Col>
          <Col size={10} style={styles.ratingCol}>
            <AverageRating rating={user.ratingAvg}/>
          </Col>
          <Col size={20} style={styles.imageCol}>
            {user.imageUrl != undefined ? <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.image}/> : null}
          </Col>
        </Row>
      </Body>
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        {/*  <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateUserDetails`)}>
          <UserStatusControl/>
        </ListItem>*/}
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateUserDetails`)}>
          <Text style={styles.text}>Personal details</Text>
          <Icon style={styles.icon} name='one-person'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/PartnerMyOrders`)}>
          <Text style={styles.text}>Completed and Rejected Jobs</Text>
          <Icon style={{paddingRight: 10}} name='two-people'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdatePaymentCardDetails`)}>
          <Text style={styles.text}>Payment cards</Text>
          <Icon style={{paddingRight: 10}} name='payment'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateBankAccountDetails`)}>
          <Text style={styles.text}>Bank Details</Text>
          <Icon style={{paddingRight: 10, fontSize: 26}} name='bank-account'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/ConfigureServices`)}>
          <Text style={styles.text}>Configure Services</Text>
          <Icon style={styles.icon} name='cog'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => Linking.openURL(`mailto:accounts@shotgun.ltd?subject=${feedbackSubject}`)}>
          <Text style={styles.text}>Give us feedback</Text>
          <Icon style={styles.icon} name='feedback'/>
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
  imageCol: {
    alignItems: 'flex-end'
  },
  ratingCol: {
    alignItems: 'center',
    justifyContent: 'center'
  },
  image: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 50,
    marginTop: 5
  },
  icon: {
    fontSize: 24,
    paddingRight: 10
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  user: getDaoState(state, ['user'], 'userDao'),
});

export default connect(
  mapStateToProps, true, false
)(PartnerSettings);


