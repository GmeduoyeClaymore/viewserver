import React from 'react';
import {Form, Text, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import yup from 'yup';
import {merge} from 'lodash';
import ValidatingInput from '../components/ValidatingInput';
import ValidatingButton from '../components/ValidatingButton';
import PlacesInput from 'common/components/maps/PlacesInput';
import MapService from 'common/services/MapService';


export default AddressDetails  = ({context, history, match, client}) => {
  const {deliveryAddress = {}} = context.state;

  const onLocationSelect = (details) => {
    const newLocation = MapService.parseGooglePlacesData(details);
    context.setState({deliveryAddress: newLocation});
  };

  const closeInputs = () => {
    this.originInput.addressInput();
  };

  const onChangeText = async (field, value) => {
    context.setState({deliveryAddress: merge(deliveryAddress, {[field]: value})});
  };

  return <Container style={{flex: 1}} >
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Address Details</Title></Body>
    </Header>

    <Form onPress={closeInputs} style={styles.form}>
      <ValidatingInput placeholder="Flat number/Business Name" value={deliveryAddress.flatNumber} onChangeText={(value) => onChangeText('flatNumber', value)} validationSchema={AddressDetails.validationSchema.flatNumber} maxLength={30}/>
      <ValidatingInput placeholder="City" value={deliveryAddress.city} onChangeText={(value) => onChangeText('city', value)} validationSchema={AddressDetails.validationSchema.city} maxLength={30}/>
      <ValidatingInput placeholder="Postcode" value={deliveryAddress.postCode} onChangeText={(value) => onChangeText('postCode', value)} validationSchema={AddressDetails.validationSchema.postCode} maxLength={30}/>
      <ValidatingInput placeholder="Country" value={deliveryAddress.country} onChangeText={(value) => onChangeText('country', value)} validationSchema={AddressDetails.validationSchema.country} maxLength={30}/>
      <ValidatingButton onPress={() => history.push(`${match.path}/PaymentCardDetails`)} validationSchema={yup.object(AddressDetails.validationSchema)} model={deliveryAddress}>
        <Text>Confirm</Text>
      </ValidatingButton>
    </Form>
    <PlacesInput ref={c => {this.addressInput = c;}} client={client} onSelect={onLocationSelect} value={deliveryAddress.line1} style={styles.addressInput}  placeholder='Search for your home address'/>
  </Container>;
};

const styles = {
  addressInput: {
    container: {
      flex: 1,
      height: 250,
      margin: 2,
      position: 'absolute',
      top: 50,
      left: 0,
      right: 0
    },
    textInput: {
      borderWidth: 0,
      fontSize: 17
    }
  },
  form: {
    flex: 1,
    position: 'absolute',
    top: 90,
    left: 0,
    right: 0
  }
};

AddressDetails.validationSchema = {
  flatNumber: yup.string().max(30),
  line1: yup.string().required().max(30),
  city: yup.string().required().max(30),
  country: yup.string().required().max(30),
  postCode: yup.string()
    .matches(/^([A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2})$/i)
    .required()
};
