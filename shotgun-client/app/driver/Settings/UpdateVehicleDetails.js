import React, {Component} from 'react';
import {Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getDaoState, getOperationErrors} from 'common/dao';
import {updateVehicle, getVehicleDetails} from 'driver/actions/DriverActions';

class UpdateVehicleDetails extends Component {
  setRegistrationNumber = (registrationNumber) => {
    this.setState({registrationNumber});
  }

  requestVehicleDetails = () => {
    const {dispatch, registrationNumber} = this.props;
    dispatch(getVehicleDetails(registrationNumber));
  }

  onUpdateVehicle = async() => {
    const {dispatch, vehicle, history, parentPath} = this.props;
    dispatch(updateVehicle(vehicle, () => history.push(`${parentPath}/DriverSettingsLanding`)));
  };

  render() {
    const {history, busy, getVehicleDetailsBusy, vehicle, errors, registrationNumber} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Vehicle Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Vehicle registration</Label>
                <ValidatingInput bold placeholder='AB01 CDE' value={registrationNumber}
                  validateOnMount={registrationNumber !== undefined}
                  onChangeText={(value) => this.setRegistrationNumber(value)}
                  validationSchema={validationSchema.registrationNumber} maxLength={10}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <ValidatingButton fullWidth padded busy={getVehicleDetailsBusy} onPress={this.requestVehicleDetails}
                validationSchema={validationSchema.registrationNumber}
                model={registrationNumber || ''}><Text uppercase={false}>Look up new vehicle</Text></ValidatingButton>
            </Col>
          </Row>
          {vehicle.make != undefined ? (<Row>
            <Col width={60}>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle registration</Label>
                <Text>{vehicle.registrationNumber}</Text>
              </Item></Row>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle model</Label>
                <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
              </Item></Row>
            </Col>
            <Col width={60}>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle colour</Label>
                <Text>{vehicle.colour}</Text>
              </Item></Row>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Approx dimensions</Label>
                <Text>{`${vehicle.dimensions.volume}m\xB3 load space`}</Text>
                <Text>{`${vehicle.dimensions.weight}kg Max`}</Text>
              </Item></Row>
            </Col>
          </Row>) : null}
        </Grid>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottom fullWidth iconRight busy={busy}
        onPress={this.onUpdateVehicle}
        validationSchema={yup.object(validationSchema)} model={vehicle}>
        <Text uppercase={false}>Update vehicle</Text>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const mapStateToProps = (state, nextOwnProps) => {
  const currentVehicle = getDaoState(state, ['vehicle'], 'vehicleDao');
  const vehicleDetails =  getDaoState(state, ['vehicleDetails'], 'driverDao');
  const {vehicleId} = currentVehicle;
  const vehicle = {...(vehicleDetails || currentVehicle), vehicleId};

  return {
    ...nextOwnProps,
    getVehicleDetailsBusy: isAnyOperationPending(state, [{driverDao: 'getVehicleDetails'}]),
    busy: isAnyOperationPending(state, [{vehicleDao: 'addOrUpdateVehicle'}]),
    errors: getOperationErrors(state, [{vehicleDao: 'addOrUpdateVehicle'}, {driverDao: 'getVehicleDetails'}]),
    vehicle
  };
};

export default withExternalState(mapStateToProps)(UpdateVehicleDetails);
