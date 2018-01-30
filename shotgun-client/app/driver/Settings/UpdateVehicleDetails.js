import React, {Component} from 'react';
import {Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Icon, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import {withRouter} from 'react-router';
import {isAnyOperationPending, getDaoState, getOperationErrors} from 'common/dao';
import {updateVehicle} from 'driver/actions/DriverActions';
import Logger from 'common/Logger';

class UpdateVehicleDetails extends Component {
  constructor(props) {
    super(props);

    this.state = {
      vehicle: {...props.vehicle, newRegistrationNumber: undefined}
    };
  }

  render() {
    const {dispatch, history, busy, client, errors} = this.props;
    const {vehicle} = this.state;
    const {dimensions} = vehicle;

    const onChangeText = async (field, value) => {
      this.setState({vehicle: merge(vehicle, {[field]: value})});
    };

    const onUpdateVehicle = async() => {
      dispatch(updateVehicle(vehicle, () => history.push('/Driver/Settings')));
    };

    const requestUpdateVehicleDetails = async () => {
      try {
        const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.newRegistrationNumber);
        this.setState({vehicle: vehicleDetails, errors: ''});
      } catch (error) {
        Logger.debug(errors);
        this.setState({errors: error});
      }
    };

    const printDimensions = () => {
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
              <Item stackedLabel first>
                <Label>Vehicle registration</Label>
                <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.newRegistrationNumber}
                  validateOnMount={vehicle.newRegistrationNumber !== undefined}
                  onChangeText={(value) => onChangeText('newRegistrationNumber', value)}
                  validationSchema={validationSchema.registrationNumber} maxLength={10}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <ValidatingButton fullWidth padded onPress={requestUpdateVehicleDetails}
                validationSchema={validationSchema.registrationNumber}
                model={vehicle.newRegistrationNumber || ''}><Text uppercase={false}>Look up new vehicle</Text></ValidatingButton>
            </Col>
          </Row>
          {vehicle.make != undefined ? (<Row>
            <Col>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle registration</Label>
                <Text>{vehicle.registrationNumber}</Text>
              </Item></Row>
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
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight busy={busy}
          onPress={onUpdateVehicle}
          validationSchema={yup.object(validationSchema)} model={vehicle}>
          <Text uppercase={false}>Update vehicle</Text>
        </ValidatingButton>
      </ErrorRegion>
    </Container>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required().matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const mapStateToProps = (state, nextOwnProps) => ({
  ...nextOwnProps,
  busy: isAnyOperationPending(state, [{vehicleDao: 'addOrUpdateVehicle'}]),
  errors: getOperationErrors(state, [{vehicleDao: 'addOrUpdateVehicle'}]),
  vehicle: getDaoState(state, ['vehicle'], 'vehicleDao')
});

export default withRouter(connect(mapStateToProps)(UpdateVehicleDetails));
