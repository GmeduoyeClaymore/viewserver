import React from 'react';
import {Text, Header, Left, Body, Container, Button, Icon, Title, Content, Grid, Row, Col, Item, Label, Input} from 'native-base';
import yup from 'yup';
import {merge} from 'lodash';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import PlacesInput from 'common/components/maps/PlacesInput';
import MapUtils from 'common/services/MapUtils';
import shotgun from 'native-base-theme/variables/shotgun';

export default AddressDetails  = ({context, history, client}) => {
  const {deliveryAddress = {}} = context.state;

  const onLocationSelect = (details) => {
    const newLocation = MapUtils.parseGooglePlacesData(details);
    context.setState({deliveryAddress: newLocation});
  };

  const closeInputs = () => {
    this.originInput.addressInput();
  };

  const onChangeText = async (field, value) => {
    context.setState({deliveryAddress: merge(deliveryAddress, {[field]: value})});
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Address Details</Title></Body>
    </Header>
    <Content onPress={closeInputs}>
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel first>
              <Label>Street Address</Label>
              <Input/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Flat number/Business Name</Label>
              <ValidatingInput bold value={deliveryAddress.flatNumber} validateOnMount={deliveryAddress.flatNumber !== undefined} onChangeText={(value) => onChangeText('flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>City</Label>
              <ValidatingInput bold value={deliveryAddress.city} validateOnMount={deliveryAddress.city !== undefined} onChangeText={(value) => onChangeText('city', value)} validationSchema={validationSchema.city} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel>
              <Label>Postcode</Label>
              <ValidatingInput bold value={deliveryAddress.postCode} validateOnMount={deliveryAddress.postCode !== undefined} onChangeText={(value) => onChangeText('postCode', value)} validationSchema={validationSchema.postCode} maxLength={30}/>
            </Item>
          </Col>
        </Row>
        <Row>
          <Col>
            <Item stackedLabel last>
              <Label>Country</Label>
              <ValidatingInput bold value={deliveryAddress.country} validateOnMount={deliveryAddress.country !== undefined} onChangeText={(value) => onChangeText('country', value)} validationSchema={validationSchema.country} maxLength={30}/>
            </Item>
          </Col>
        </Row>
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push('/Customer/Registration/PaymentCardDetails')} validationSchema={yup.object(validationSchema)} model={deliveryAddress}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
    <PlacesInput ref={c => {this.addressInput = c;}} client={client} onSelect={onLocationSelect} value={deliveryAddress.line1} style={styles.addressInput}  placeholder='Search for your home address'/>
  </Container>;
};

const validationSchema = {
  flatNumber: yup.string().max(30),
  line1: yup.string().required().max(30),
  city: yup.string().required().max(30),
  country: yup.string().required().max(30),
  postCode: yup.string()
    .matches(/^([A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2})$/i)
    .required()
};

const styles = {
  addressInput: {
    container: {
      flex: 1,
      height: 250,
      margin: 2,
      position: 'absolute',
      top: 88,
      left: shotgun.contentPadding - 3,
      right: 0,
      borderBottomWidth: 0
    },
    textInputContainer: {
      backgroundColor: '#FFFFFF',
      borderBottomWidth: 0,
      borderTopWidth: 0
    },
    textInput: {
      borderWidth: 0,
      fontWeight: 'bold',
      fontSize: 18
    }
  }
};
