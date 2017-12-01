import React from 'react';
import {Picker} from 'react-native';
import { Form, Text, Content, Header, Body, Container, Title, Item, Label, Input} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import {getDaoState} from 'common/dao';
import { withRouter } from 'react-router';

const VehicleDetails  = ({context, history, vehicleTypes}) => {
  const {vehicle = {}} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({vehicle: merge(vehicle, {[field]: value})});
  };

  const currentVehicleType = vehicleTypes.find(v => v.vehicleTypeId == vehicle.vehicleTypeId);

  return <Container>
    <Header>
      <Body><Title>Vehicle Details</Title></Body>
    </Header>
    <Content>
      <Form style={{display: 'flex', flex: 1}}>
        <ValidatingInput placeholder="Registration Number" value={vehicle.registrationNumber} onChangeText={(value) => onChangeText('registrationNumber', value)} validationSchema={VehicleDetails.validationSchema.registrationNumber} maxLength={10}/>
        <Item fixedLabel>
          <Label>Colour:</Label>
          <Input value={vehicle.colour} editable={false}/>
        </Item>
        <Item fixedLabel>
          <Label>Make & Model:</Label>
          <Input value={`${vehicle.make} ${vehicle.model}`} editable={false}/>
        </Item>
        <Picker selectedValue={vehicle.vehicleTypeId} onValueChange={(itemValue) => onChangeText('vehicleTypeId', itemValue)}>
          <Picker.Item label="--Select Vehicle Type--"/>
          {vehicleTypes.map(c => <Picker.Item  key={c.vehicleTypeId} label={c.bodyType} value={c.vehicleTypeId} />)}
        </Picker>
        <Text>{currentVehicleType ? currentVehicleType.description : null}</Text>
        <ValidatingButton onPress={() => history.push('/Driver/Registration/DriverRegistrationConfirmation')} validationSchema={yup.object(VehicleDetails.validationSchema)} model={vehicle}>
          <Text>Next</Text>
        </ValidatingButton>
      </Form>
    </Content>
  </Container>;
};

VehicleDetails.validationSchema = {
  registrationNumber: yup.string().required(), //TODO - number plate regex
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  vehicleTypeId: yup.string().required()
};


const mapStateToProps = (state, initialProps) => ({
  vehicleTypes: getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(VehicleDetails));
