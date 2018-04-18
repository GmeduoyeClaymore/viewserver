import React, {Component} from 'react';
import {View, ScrollView} from 'react-native';
import {Text, Item, Label, Button, Grid, Row, Col, Container, Content} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyLoading, getLoadingErrors, getOperationError } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import ValidationService from 'common/services/ValidationService';

class DriverCapabilityDetails extends Component{
  constructor(props){
    super(props);
    this.onChangeText = this.onChangeText.bind(this);
    this.togglePeopleVisibility = this.togglePeopleVisibility.bind(this);
    this.requestVehicleDetails = this.requestVehicleDetails.bind(this);
  }

  onChangeText(field, value){
    const {vehicle = {}} = this.props;
    this.setState({vehicle: {...vehicle, [field]: value}});
  }

  async requestVehicleDetails(){
    const {client, vehicle} = this.props;
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.registrationNumber);
      this.setState({vehicle: vehicleDetails, errors: '', selectedProductIds: vehicleDetails.selectedProductIds});
    } catch (error){
      this.setState({vehicle: {registrationNumber: vehicle.registrationNumber}, errors: error});
    }
  }

  togglePeopleVisibility(peopleVisible){
    this.onChangeText('numAvailableForOffload', peopleVisible ? 1 : undefined);
  }

  render(){
    const {dimensions, errors, vehicle} = this.props;
    const {numAvailableForOffload} = vehicle;
    const peopleVisible = numAvailableForOffload > 0;
    const combinedErrors = `${errors}`;

    return <Content><Grid>
      <Row>
        <Col>
          <Item stackedLabel style={{marginLeft: 0, borderBottomWidth: 0, width: '100%'}}>
            <Label>Vehicle registration</Label>
            <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.registrationNumber} validateOnMount={vehicle.registrationNumber !== undefined} onChangeText={(value) => this.onChangeText('registrationNumber', value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
          </Item>
          <ValidatingButton fullWidth onPress={() => this.requestVehicleDetails()} validateOnMount={vehicle.registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={vehicle.registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
        </Col>
      </Row>

      {vehicle.make != undefined ? (<Row style={{flexWrap: 'wrap'}}>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle model</Label>
          <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle colour</Label>
          <Text>{vehicle.colour}</Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Approx dimensions</Label>
          <Text>{`${dimensions.volume}m\xB3 load space`}</Text>
          <Text>{`${dimensions.weight}kg Max`}</Text>
        </Item></Col>
      </Row>) : null}
      <Row>
        <Col>
          <Grid style={{marginBottom: shotgun.contentPadding, marginTop: 20}}>
            <Row><Text style={{marginBottom: shotgun.contentPadding}}>Are you able to load and off-load items?</Text></Row>
            <Row>
              <Col style={{paddingRight: shotgun.contentPadding}}><Button fullWidth light active={peopleVisible} onPress={() => this.togglePeopleVisibility(true)}><Text uppercase={false}>Yes</Text></Button></Col>
              <Col><Button fullWidth light active={!peopleVisible} onPress={() => this.togglePeopleVisibility(false)}><Text uppercase={false}>No</Text></Button></Col>
            </Row>
          </Grid>
          {peopleVisible ?
            <Grid>
              <Row>
                <Text style={{paddingBottom: 15}}>How many people will be available?</Text>
              </Row>
              <Row>
                <Col style={{marginRight: 10}}>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 1} onPress={() => this.onChangeText('numAvailableForOffload', 1)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>1</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
                <Col style={{marginRight: 10}}>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 2} onPress={() => this.onChangeText('numAvailableForOffload', 2)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>2</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
                <Col>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 3} onPress={() => this.onChangeText('numAvailableForOffload', 3)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>3</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
              </Row>
            </Grid> : null}
        </Col>
      </Row>
      <ErrorRegion errors={combinedErrors}/>
    </Grid></Content>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const styles = {
  personSelectTextRow: {
    justifyContent: 'center'
  },
  personSelectText: {
    marginTop: 5,
    fontWeight: 'normal',
    fontSize: 16,
    textAlign: 'center'
  }
};

const mapStateToProps = (state, initialProps) => {
  const {vehicle = {}, selectedContentTypes, user, bankAccount, address, errors} = initialProps;
  const {dimensions} = vehicle;
  const registrationErrors = getOperationError(state, 'loginDao', 'registerAndLoginDriver') || [];
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy = isAnyLoading(state, ['userDao', 'vehicleDao', 'loginDao']);
  return {
    ...initialProps,
    vehicle,
    user,
    bankAccount,
    address,
    selectedContentTypes,
    dimensions,
    busy,
    errors: [loadingErrors, errors, registrationErrors].filter( c=> !!c).join('\n')
  };
};

const  canSubmit = async (state) => {
  const {vehicle} = state;
  return await ValidationService.validate(vehicle, yup.object(validationSchema));
};

export const ConnectedDriverCapabilityDetails =  withExternalState(mapStateToProps)(DriverCapabilityDetails);
export default {control: ConnectedDriverCapabilityDetails, validator: canSubmit};
