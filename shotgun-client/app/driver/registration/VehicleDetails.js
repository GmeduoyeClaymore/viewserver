import React from 'react';
import { Form, Text, Content, Header, Body, Container, Title, Item, Label} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import { withRouter } from 'react-router';
import { debounce } from 'lodash';


export const printDimensions = (vehicle) =>{
  if (!vehicle || !vehicle.dimensions){
    return null;
  }
  const {dimensions} = vehicle;
  return `${dimensions.height}  x ${dimensions.width} x ${dimensions.length}`;
};

export const printWeight = (vehicle) =>{
  if (!vehicle || !vehicle.dimensions){
    return null;
  }
  const {dimensions} = vehicle;
  return `${dimensions.weight}kg Max`;
};

const requestVehicleDetails = (client, context, field, value) => async () => {
  if (field === 'registrationNumber'){
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', value);
      context.setState({vehicle: vehicleDetails, errors: ''});
    } catch (error){
      context.setState({errors: error});
    }
  }
};

const VehicleDetails  = ({context, history, client}) => {
  const {vehicle = {}, errors} = context.state;

  const onChangeText = async (field, value) => {
    context.setState({vehicle: merge(vehicle, {[field]: value})}, debounce(requestVehicleDetails(client, context, field, value), 500) );
  };

  return <Container>
    <Header>
      <Body><Title>Vehicle Details</Title></Body>
    </Header>
    <Content>
      <Form style={{display: 'flex', flex: 1}}>
        <ErrorRegion errors={errors}>
          <ValidatingInput placeholder="Registration Number" value={vehicle.registrationNumber} onChangeText={(value) => onChangeText('registrationNumber', value)} validationSchema={VehicleDetails.validationSchema.registrationNumber} maxLength={10}/>
        </ErrorRegion>
        <Item fixedLabel>
          <Label>Make & Model:</Label>
          <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
        </Item>
        <Item fixedLabel>
          <Label>Colour:</Label>
          <Text>{vehicle.colour}  </Text>
        </Item>
        <Item fixedLabel>
          <Label>Approx Dimensions:</Label>
          <Text>{printDimensions(vehicle)}</Text>
          <Text>{printWeight(vehicle)}</Text>
        </Item>
        
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
  dimensions: yup.object().required(),
  vehicleTypeId: yup.string().required()
};


const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(VehicleDetails));
