import React from 'react';
import {Button, Text, Content, List, ListItem, Header, Container, Body, Title, Row, Col} from 'native-base';
import {connect} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {Icon, AverageRating} from 'common/components';
import {logOut, unregisterAllDaosAndResetComponentState} from 'common/actions/CommonActions';
import {View, Linking} from 'react-native';
//import DeviceInfo from 'react-native-device-info';

const feedbackSubject = '';//`Customer Feedback from ${DeviceInfo.getApplicationName()} version ${DeviceInfo.getReadableVersion()} running on ${DeviceInfo.getModel()}${DeviceInfo.isEmulator() ? ' emulator' : ''} ${DeviceInfo.getSystemName()} ${DeviceInfo.getSystemVersion()}`;

const CustomerSettings = ({history, user = {}, parentPath, dispatch}) => {
  const signOut = async () => {
    dispatch(logOut(() => history.push('/')));
    dispatch(unregisterAllDaosAndResetComponentState());
  };

  return user ? <Container>
    <Header>
      <Body>
        <Row style={{width: '100%'}}>
          <Col size={75}>
            <Title>{user.firstName} {user.lastName}</Title>
          </Col>
          <Col size={25} style={{paddingTop: 20}}>
           <AverageRating rating={user.ratingAvg}/>
          </Col>
        </Row>
      </Body>
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateUserDetails`)}>
          <Text style={styles.text}>Personal details</Text>
          <Icon style={styles.icon} name='one-person'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdateAddressDetails`)}>
          <Text style={styles.text}>Home address</Text>
          <Icon style={styles.icon} name='address'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push(`${parentPath}/UpdatePaymentCardDetails`)}>
          <Text style={styles.text}>Payment cards</Text>
          <Icon style={{paddingRight: 10}} name='payment'/>
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
)(CustomerSettings);


