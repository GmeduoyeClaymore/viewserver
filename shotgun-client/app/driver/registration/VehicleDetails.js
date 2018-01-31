import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'custom-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import {withRouter} from 'react-router';
import {Icon} from 'common/components/Icon';
import { getDaoState, isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError } from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import {registerDriver} from 'driver/actions/DriverActions';
import LoadingScreen from 'common/components/LoadingScreen';
import shotgun from 'native-base-theme/variables/shotgun';

class VehicleDetails extends Component{
  constructor(props){
    super(props);
    this.selectContentType = this.selectContentType.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.renderContentType = this.renderContentType.bind(this);
    this.requestVehicleDetails = this.requestVehicleDetails.bind(this);
    this.register = this.register.bind(this);
  }

  onChangeText(field, value){
    const {vehicle, context} = this.props;
    context.setState({vehicle: merge(vehicle, {[field]: value})});
  }

  async register(){
    const {user, vehicle, bankAccount, address, selectedContentTypes, dispatch, history} = this.props;
    user.selectedContentTypes = selectedContentTypes.join(',');
    dispatch(registerDriver(user, vehicle, address, bankAccount, () => history.push('/Root')));
  }


  selectContentType(selectedContentType){
    let {context, selectedContentTypes = []} = this.props;
    const index = selectedContentTypes.indexOf(selectedContentType.contentTypeId);
    if (!!~index){
      selectedContentTypes = selectedContentTypes.filter((_, idx) => idx !== index);
    } else {
      selectedContentTypes = [...selectedContentTypes, selectedContentType.contentTypeId];
    }
    context.setState({selectedContentTypes});
  }

  renderContentType(contentType, i){
    const {selectedContentTypes = []} = this.props;
    return <View key={i} style={{width: '30%'}}>
      <Button style={{height: 'auto'}} large active={!!~selectedContentTypes.indexOf(contentType.contentTypeId)} onPress={() => this.selectContentType(contentType)}>
        <Icon name='car'/>
      </Button>
      <Text style={styles.productSelectTextRow}>{contentType.name}</Text>
    </View>;
  }

  async requestVehicleDetails(){
    const {context, client, vehicle} = this.props;
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.registrationNumber);
      context.setState({vehicle: vehicleDetails, errors: ''});
    } catch (error){
      context.setState({errors: error});
    }
  }

  onChangeValue(field, value){
    const {vehicle, context} = this.props;
    context.setState({vehicle: merge({}, vehicle, {[field]: value})});
  }


  printDimensions(){
    const {dimensions} = this.props;
    return `${Math.floor(dimensions.height / 1000)}m  x ${Math.floor(dimensions.width / 1000)}m x ${Math.floor(dimensions.length / 1000)}m`;
  }

  render(){
    const {history, contentTypes = [], vehicle = {}, dimensions, selectedContentTypes = [], errors, busy} = this.props;
    const {numAvailableForOffload} = vehicle;
    const containsDelivery = !!~selectedContentTypes.indexOf(ContentTypes.DELIVERY);

    return  busy ? <LoadingScreen text="Registering You With Shotgun"/> : <Container>
    <Header withButton>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()}/>
        </Button>
      </Left>
        <Body><Title>Account Type</Title></Body>
    </Header>
      <Content keyboardShouldPersistTaps="always" padded>
      <Grid>
        <Row>
          <Col>
              <View style={{...styles.productSelectView}}>
                <Grid>
                  <Row style={{flexWrap: 'wrap'}}>
                    {contentTypes.map((v, i) => this.renderContentType(v, i))}
                  </Row>
                </Grid>
              </View>
            </Col>
          </Row>
        </Grid>
      </Content>

      { containsDelivery ? <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
            <ErrorRegion errors={errors}>
              <Item stackedLabel first>
                <Label>Vehicle registration</Label>
                  <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.registrationNumber} validateOnMount={vehicle.registrationNumber !== undefined} onChangeText={(value) => this.onChangeText('registrationNumber', value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
              </Item>
            </ErrorRegion>
          </Col>
        </Row>
        <Row>
          <Col>
              <ValidatingButton fullWidth padded onPress={() => this.requestVehicleDetails()} validateOnMount={vehicle.registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={vehicle.registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
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
                <Text>{this.printDimensions()}</Text>
              <Text>{`${dimensions.weight}kg Max`}</Text>
            </Item></Row>
          </Col>
        </Row>) : null}
      </Grid>
        <Row>
          <Col>
            <Content padded keyboardShouldPersistTaps="always">
              <Grid style={{marginBottom: shotgun.contentPadding}}>
                <Row><Text style={{marginBottom: shotgun.contentPadding}}>Are you able to load and off-load items?</Text></Row>
                <Row>
                  <Col style={{paddingRight: shotgun.contentPadding}}><Button fullWidth light active={numAvailableForOffload > 0} onPress={() => this.onChangeValue('numAvailableForOffload', 1)}><Text uppercase={false}>Yes</Text></Button></Col>
                  <Col><Button fullWidth light active={numAvailableForOffload == 0} onPress={() => this.onChangeValue('numAvailableForOffload', 0)}><Text uppercase={false}>No</Text></Button></Col>
                </Row>
              </Grid>
              {numAvailableForOffload > 0 ?
                <Grid>
                  <Row>
                    <Text style={{paddingBottom: 15}}>How many people will be available?</Text>
                  </Row>
                  <Row>
                    <Col style={{marginRight: 10}}>
                      <Row>
                        <Button personButton active={numAvailableForOffload == 1} onPress={() => this.onChangeValue('numAvailableForOffload', 1)} >
                          <Icon name='man'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>1</Text>
                      </Row>
                    </Col>
                    <Col style={{marginRight: 10}}>
                      <Row>
                        <Button personButton active={numAvailableForOffload == 2} onPress={() => this.onChangeValue('numAvailableForOffload', 2)} >
                          <Icon name='man'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>2</Text>
                      </Row>
                    </Col>
                    <Col>
                      <Row>
                        <Button personButton active={numAvailableForOffload == 3} onPress={() => this.onChangeValue('numAvailableForOffload', 3)} >
                          <Icon name='man'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>3</Text>
                      </Row>
                    </Col>
                  </Row>
                </Grid> : null}
    </Content>
          </Col>
        </Row>
      </Content> : null}
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={this.register} validationSchema={yup.object(validationSchema)} model={vehicle}>
          <Text uppercase={false}>Register</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
      </ErrorRegion>
  </Container>;
  }
}

const styles = {
  h1: {
    width: '80%',
    marginBottom: 30
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center'
  },
  titleView: {
    flex: 1,
    justifyContent: 'flex-end'
  },
  productSelectView: {
    flex: 2,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  productSelectTextRow: {
    justifyContent: 'center'
  },
  productSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    width: '80%',
    textAlign: 'center'
  }
};


const validationSchema = {
  registrationNumber: yup.string().required().matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const mapStateToProps = (state, initialProps) => {
  const {context = {}} = initialProps;
  const {vehicle = {}, errors = [], user, bankAccount, address, selectedContentTypes} = context.state;
  const {dimensions} = vehicle;
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const registrationErrors = getOperationError(state, 'driverDao', 'registerDriver') || [];
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy =  isAnyLoading(state, ['contentTypeDao']) || isAnyOperationPending(state, [{ driverDao: 'registerDriver'}] || isAnyLoading(state, ['userDao', 'vehicleDao', 'driverDao']));
  return {
    ...initialProps,
    context,
    user,
    vehicle,
    bankAccount,
    address,
    selectedContentTypes,
    dimensions,
    contentTypes,
    busy,
    errors: [loadingErrors, errors, registrationErrors].filter( c=> !!c).join('\n')
  };
};


export default withRouter(connect(mapStateToProps)(VehicleDetails));
