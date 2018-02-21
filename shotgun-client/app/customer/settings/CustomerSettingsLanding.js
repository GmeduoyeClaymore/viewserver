import React from 'react';
import { Button, Text, Content, List, ListItem, Header, Container, Left, Body, Title, Subtitle} from 'native-base';
import {connect} from 'custom-redux';
import {getDaoState} from 'common/dao';
import { withRouter } from 'react-router';
import PrincipalService from 'common/services/PrincipalService';
import {Icon} from 'common/components';

const CustomerSettings = ({history, user}) => {
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
    </Header>
    <Content padded keyboardShouldPersistTaps="always">
      <List>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Customer/Settings/UpdateUserDetails')}>
          <Text style={styles.text}>Personal details</Text>
          <Icon style={styles.icon} name='one-person'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Customer/Settings/UpdateAddressDetails')}>
          <Text style={styles.text}>Home address</Text>
          <Icon style={styles.icon} name='address'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/Customer/Settings/UpdatePaymentCardDetails')}>
          <Text style={styles.text}>Payment cards</Text>
          <Icon style={{paddingRight: 10}} name='payment'/>
        </ListItem>
        <ListItem paddedTopBottom iconRight onPress={() => history.push('/')}>
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
)(CustomerSettings));


