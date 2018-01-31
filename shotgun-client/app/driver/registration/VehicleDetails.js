import React from 'react';
import {Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import {withRouter} from 'react-router';
import {Icon} from 'common/components/Icon';

const VehicleDetails  = ({context, history, client}) => {
  const {vehicle = {}, errors} = context.state;
  const {dimensions} = vehicle;

  const onChangeText = async (field, value) => {
    context.setState({vehicle: merge(vehicle, {[field]: value})});
  };

  const requestVehicleDetails = async() => {
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.registrationNumber);
      context.setState({vehicle: vehicleDetails, errors: ''});
    } catch (error){
      context.setState({errors: error});
    }
  };

  const printDimensions = () =>{
    return `${Math.floor(dimensions.height / 1000)}m  x ${Math.floor(dimensions.width / 1000)}m x ${Math.floor(dimensions.length / 1000)}m`;
  };

  return <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()}/>
        </Button>
      </Left>
      <Body><Title>Vehicle Details</Title></Body>
    </Header>
    <Content keyboardShouldPersistTaps="always">
      <Grid>
        <Row>
          <Col>
            <ErrorRegion errors={errors}>
              <Item stackedLabel first>
                <Label>Vehicle registration</Label>
                <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.registrationNumber} validateOnMount={vehicle.registrationNumber !== undefined} onChangeText={(value) => onChangeText('registrationNumber', value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
              </Item>
            </ErrorRegion>
          </Col>
        </Row>
        <Row>
          <Col>
            <ValidatingButton fullWidth padded onPress={() => requestVehicleDetails()} validateOnMount={vehicle.registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={vehicle.registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
          </Col>
        </Row>
        {vehicle.make != undefined ? (<Row>
          <Col>
            <Row><Item stackedLabel vehicleDetails>
              <Label>Vehicle model</Label>
              <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
            </Item></Row>
            <Row><Item stackedLabel vehicleDetails>
              <Label>Vehicle colour</Label>
              <Text>{vehicle.colour}</Text>
            </Item></Row>
            <Row><Item stackedLabel vehicleDetails>
              <Label>Approx dimensions</Label>
              <Text>{printDimensions()}</Text>
              <Text>{`${dimensions.weight}kg Max`}</Text>
            </Item></Row>
          </Col>
        </Row>) : null}
      </Grid>
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push('/Driver/Registration/OffloadDetails')} validationSchema={yup.object(validationSchema)} model={vehicle}>
      <Text uppercase={false}>Continue</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
  </Container>;
};

const validationSchema = {
  registrationNumber: yup.string().required().matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

export default withRouter(connect()(VehicleDetails));
