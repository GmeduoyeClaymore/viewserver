import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Item, Label, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {withRouter} from 'react-router';
import {isAnyLoading, getLoadingErrors, getOperationError } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import ValidationService from 'common/services/ValidationService';

class DeliveryDetails extends Component{
  constructor(props){
    super(props);
    this.onChangeText = this.onChangeText.bind(this);
    this.togglePeopleVisibility = this.togglePeopleVisibility.bind(this);
    this.requestVehicleDetails = this.requestVehicleDetails.bind(this);
  }

  static validationSchema = {
    registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
    colour: yup.string().required(),
    make: yup.string().required(),
    model: yup.string().required(),
    dimensions: yup.object().required()
  };

  static async canSubmit(state){
    const {vehicle} = state;
    return await ValidationService.validate(vehicle, yup.object(DeliveryDetails.validationSchema));
  }
  
  onChangeText(field, value){
    const {context, vehicle = {}} = this.props;
    context.setState({vehicle: {...vehicle, [field]: value}});
  }

  async requestVehicleDetails(){
    const {context, client, vehicle} = this.props;
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.registrationNumber);
      context.setState({vehicle: vehicleDetails, errors: '', selectedProductIds: vehicleDetails.selectedProductIds});
    } catch (error){
      context.setState({vehicle: {registrationNumber: vehicle.registrationNumber}, errors: error});
    }
  }

  printDimensions(){
    const {dimensions} = this.props;
    return `${Math.floor(dimensions.height / 1000)}m  x ${Math.floor(dimensions.width / 1000)}m x ${Math.floor(dimensions.length / 1000)}m`;
  }

  togglePeopleVisibility(peopleVisible){
    this.onChangeText('numAvailableForOffload', peopleVisible ? 1 : undefined);
  }

  render(){
    const {dimensions, errors, vehicle, context} = this.props;
    const {numAvailableForOffload} = vehicle;
    const peopleVisible = numAvailableForOffload > 0;
    const combinedErrors = `${errors}\n${context.errors}`;

    return <Content keyboardShouldPersistTaps="always" padded>
      <View>
        <Row>
          <Item stackedLabel style={{marginLeft: 0, borderBottomWidth: 0, width: '100%'}}>
            <Label>Vehicle registration</Label>
            <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.registrationNumber} validateOnMount={vehicle.registrationNumber !== undefined} onChangeText={(value) => this.onChangeText('registrationNumber', value)} validationSchema={DeliveryDetails.validationSchema.registrationNumber} maxLength={10}/>
          </Item>
        </Row>
        <Row>
          <Col>
            <ValidatingButton fullWidth onPress={() => this.requestVehicleDetails()} validateOnMount={vehicle.registrationNumber !== undefined} validationSchema={DeliveryDetails.validationSchema.registrationNumber} model={vehicle.registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
          </Col>
        </Row>
        {vehicle.make != undefined ? (<Row style={{flexWrap: 'wrap'}}>
          <View style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Vehicle model</Label>
            <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
          </Item></View>
          <View style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Vehicle colour</Label>
            <Text>{vehicle.colour}</Text>
          </Item></View>
          <View style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Approx dimensions</Label>
            <Text>{this.printDimensions()}</Text>
            <Text>{`${dimensions.weight}kg Max`}</Text>
          </Item></View>
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
                        <Icon name='one-person'/>
                      </Button>
                    </Row>
                    <Row style={styles.personSelectTextRow}>
                      <Text style={styles.personSelectText}>1</Text>
                    </Row>
                  </Col>
                  <Col style={{marginRight: 10}}>
                    <Row>
                      <Button personButton active={numAvailableForOffload == 2} onPress={() => this.onChangeText('numAvailableForOffload', 2)} >
                        <Icon name='one-person'/>
                      </Button>
                    </Row>
                    <Row style={styles.personSelectTextRow}>
                      <Text style={styles.personSelectText}>2</Text>
                    </Row>
                  </Col>
                  <Col>
                    <Row>
                      <Button personButton active={numAvailableForOffload == 3} onPress={() => this.onChangeText('numAvailableForOffload', 3)} >
                        <Icon name='one-person'/>
                      </Button>
                    </Row>
                    <Row style={styles.personSelectTextRow}>
                      <Text style={styles.personSelectText}>3</Text>
                    </Row>
                  </Col>
                </Row>
              </Grid> : null}
          </Col>
        </Row>
        <ErrorRegion errors={combinedErrors}/>
      </View>
    </Content>;
  }
}

const styles = {
  personSelectTextRow: {
    justifyContent: 'center'
  },
  personSelectText: {
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

const mapStateToProps = (state, initialProps) => {
  const {context = {}, vehicle = {}, selectedContentTypes, user, bankAccount, address, errors} = initialProps;
  const {dimensions} = vehicle;
  const registrationErrors = getOperationError(state, 'driverDao', 'registerDriver') || [];
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy = isAnyLoading(state, ['userDao', 'vehicleDao', 'driverDao']);
  return {
    ...initialProps,
    context,
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


export default withRouter(connect(mapStateToProps)(DeliveryDetails));
