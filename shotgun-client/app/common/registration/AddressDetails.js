import React from 'react';
import {Text, Header, Left, Body, Container, Button, Title, Content, Grid, Row, Col, Item, Label} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {withExternalState} from 'custom-redux';

const AddressDetails  = ({deliveryAddress = {}, match, history, next, setState, dispatch}) => {
  const onAddressSelected = (address) => {
    setState({deliveryAddress: address}, () => history.goBack(), dispatch);
  };

  const onChangeText = async (field, value) => {
    setState({deliveryAddress: {...deliveryAddress, [field]: value}}, dispatch);
  };

  const doAddressLookup = (addressLabel) => {
    history.push(`${match.path}/AddressLookup`, {addressLabel, onAddressSelected });
  };

  const getLocationText = (location = {}, placeholder) => {
    const style = location.line1 ? {} : styles.locationTextPlaceholder;
    const text = location.line1 ? location.line1 : placeholder;
    return <Text style={[styles.line1Text, style]} onPress={() => doAddressLookup(placeholder)}>{text}</Text>;
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button onPress={() => history.goBack()}>
          <Icon name='back-arrow'/>
        </Button>
      </Left>
      <Body><Title>Address Details</Title></Body>
    </Header>
    <Content keyboardShouldPersistTaps="always">
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Street Address</Label>
              {getLocationText(deliveryAddress, 'Search for your home address')}
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Flat number/Business Name</Label>
              <ValidatingInput bold placeholder="Optional" value={deliveryAddress.flatNumber} validateOnMount={deliveryAddress.flatNumber !== undefined} onChangeText={(value) => onChangeText('flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>City</Label>
              <ValidatingInput bold placeholder="Cityville" value={deliveryAddress.city} validateOnMount={deliveryAddress.city !== undefined} onChangeText={(value) => onChangeText('city', value)} validationSchema={validationSchema.city} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Postcode</Label>
              <ValidatingInput bold placeholder="PC12 ABC" value={deliveryAddress.postCode} validateOnMount={deliveryAddress.postCode !== undefined} onChangeText={(value) => onChangeText('postCode', value)} validationSchema={validationSchema.postCode} maxLength={30}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push(next)} validationSchema={yup.object(validationSchema)} model={deliveryAddress}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='forward-arrow' next/>
    </ValidatingButton>
  </Container>;
};

AddressDetails.stateKey = 'checkout';

export default withExternalState()(AddressDetails);

const validationSchema = {
  flatNumber: yup.string().max(30),
  line1: yup.string().required().max(30),
  city: yup.string().required().max(30),
  postCode: yup.string()
    //.matches(/^([A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2})$/i) REGEX VALIDATOR ISSUES IN IOS
    .required()
};

const styles = {
  line1Text: {
    fontWeight: 'bold',
    fontSize: 18,
    paddingTop: 5,
    paddingBottom: 10
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  }
};
