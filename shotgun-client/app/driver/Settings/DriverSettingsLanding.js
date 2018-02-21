import React from 'react';
import {Button, Text, Content, List, ListItem, Header, Container, Left, Right, Body, Title, Subtitle} from 'native-base';
import {connect} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {Image, Linking} from 'react-native';
import {withRouter} from 'react-router';
import PrincipalService from 'common/services/PrincipalService';
import {Icon} from 'common/components';
import DeviceInfo from 'react-native-device-info';

const feedbackSubject = `Driver Feedback from ${DeviceInfo.getApplicationName()} version ${DeviceInfo.getReadableVersion()} running on ${DeviceInfo.getModel()}${DeviceInfo.isEmulator() ? ' emulator' : ''} ${DeviceInfo.getSystemName()} ${DeviceInfo.getSystemVersion()}`;

const DriverSettings = ({history, user}) => {
  const signOut = async () => {
    await PrincipalService.removeUserIdFromDevice();
    history.push('/Root');
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button onPress={() => history.goBack()}>
          <Icon name='back-arrow'/>
        </Button>
      </Left>
      <Body>
        <Title>{user.firstName} {user.lastName}</Title>
        {user.ratingAvg > 0 ? <Subtitle><Icon name='star' avgStar/> {user.ratingAvg.toFixed(1)}</Subtitle> : null}
      </Body>
      <Right>
        {user.imageUrl != undefined ? <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.image}/> : null}
      </Right>
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateUserDetails')}>
          <Text style={styles.text}>Personal details</Text>
          <Icon style={styles.icon} name='one-person'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateBankAccountDetails')}>
          <Text style={styles.text}>Bank Details</Text>
          <Icon style={{paddingRight: 10}} name='payment'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateVehicleDetails')}>
          <Text style={styles.text}>Vehicle Details</Text>
          <Icon style={styles.icon} name='drive'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => Linking.openURL(`mailto:accounts@shotgun.ltd?subject=${feedbackSubject}`)}>
          <Text style={styles.text}>Give us feedback</Text>
          <Icon style={styles.icon} name='feedback'/>
        </ListItem>
      </List>
    </Content>
    <Button fullWidth paddedBottom signOutButton onPress={signOut}><Text uppercase={false}>Sign out</Text></Button>
  </Container>;
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
    fontSize: 24,
    paddingRight: 10
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  user: getDaoState(state, ['user'], 'userDao'),
});

export default withRouter(connect(
  mapStateToProps
)(DriverSettings));


