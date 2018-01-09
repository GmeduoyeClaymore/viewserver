import React from 'react';
import {connect} from 'react-redux';
import {ScrollView} from 'react-native';
import {registerDriver, driverServicesRegistrationAction} from 'driver/actions/DriverActions';
import uuidv4 from 'uuid/v4';
import ErrorRegion from 'common/components/ErrorRegion';
import {Form, Text, Button, Item, Label, Input, Content, Header, Left, Body, Container, Icon, Title} from 'native-base';
import {isAnyLoading, isAnyOperationPending, getOperationError} from 'common/dao';
import LoadingScreen from 'common/components/LoadingScreen';

const DriverRegistrationConfirmation  = ({context, history, dispatch, client, errors, busy}) => {
  const {user, vehicle} = context.state;

  const register = async () => {
    dispatch(registerDriver(user, vehicle, () => history.push('/Root')));
  };

  const createServicesThenRegister = async () => {
    //TODO - dont like that we have to register stuff here
    dispatch(driverServicesRegistrationAction(client, uuidv4(), register));
  };

  return busy ? <LoadingScreen text="Registering You With Shotgun"/> : <Container>
    <Header>
      <Left>
        <Button transparent disabled={busy}>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Confirm Details</Title></Body>
    </Header>
    <Content>
      <ErrorRegion errors={errors}><ScrollView style={{flex: 1, flexDirection: 'column'}}>
        <Form>
          <Text>Personal Details</Text>
          <Item fixedLabel>
            <Label>First Name</Label>
            <Input value={user.firstName} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Last Name</Label>
            <Input value={user.lastName} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Email</Label>
            <Input value={user.email} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Phone Number</Label>
            <Input value={user.contactNo} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Registration</Label>
            <Input value={vehicle.registrationNumber} editable={false}/>
          </Item>
          <Item fixedLabel>
            <Label>Make & Model:</Label>
            <Input value={`${vehicle.make} ${vehicle.model}`} editable={false}/>
          </Item>
          <Button disabled={busy} onPress={createServicesThenRegister}>
            <Text>Create Account</Text>
          </Button>
        </Form>
      </ScrollView>
      </ErrorRegion>
    </Content>
  </Container>;
};

const mapStateToProps = (state, initialProps) => ({
  errors: getOperationError(state, 'driverDao', 'addOrUpdateDriver'),
  busy: isAnyOperationPending(state, { driverDao: 'addOrUpdateDriver'} || isAnyLoading(state, ['userDao', 'vehicleDao', 'driverDao'])),
  ...initialProps
});

export default connect(mapStateToProps)(DriverRegistrationConfirmation);
