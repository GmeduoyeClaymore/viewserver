import React from 'react';
import { Button, Text, Content, List, ListItem, Header, Container, Left, Body, Title, Subtitle} from 'native-base';
import {connect} from 'react-redux';
import {getDaoState} from 'common/dao';
import { withRouter } from 'react-router';
import PrincipalService from 'common/services/PrincipalService';
import {Icon} from 'common/components/Icon';

const DriverSettings = ({history, user}) => {
  const signOut = async () => {
    await PrincipalService.removeUserIdFromDevice();
    history.push('/Root');
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()}/>
        </Button>
      </Left>
      <Body><Title>{user.firstName} {user.lastName}</Title><Subtitle>{user.email}</Subtitle></Body>
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateUserDetails')}>
          <Text style={styles.text}>Personal details</Text>
          <Icon name='arrow-forward'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateBankAccountDetails')}>
          <Text style={styles.text}>Bank Details</Text>
          <Icon name='arrow-forward'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/Settings/UpdateVehicleDetails')}>
          <Text style={styles.text}>Vehicle Details</Text>
          <Icon name='arrow-forward'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Driver/UserDetails')}>
          <Text style={styles.text}>Give us feedback</Text>
          <Icon name='arrow-forward'/>
        </ListItem>
      </List>
    </Content>
    <Button fullWidth paddedBottom signOutButton onPress={signOut}><Text uppercase={false}>Sign out</Text></Button>
  </Container>;
};

const styles = {
  text: {
    fontSize: 16
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  user: getDaoState(state, ['user'], 'userDao'),
});

export default withRouter(connect(
  mapStateToProps
)(DriverSettings));


