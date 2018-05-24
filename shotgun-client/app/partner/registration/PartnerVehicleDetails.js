import React, {Component} from 'react';
import {ScrollView} from 'react-native';
import {Button, Text, Item, Label, Grid, Row, Col, Content, Spinner, View} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, PagingListView, Icon, LoadingScreen} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getDaoOptions, getOperationError, getDaoState, isAnyLoading} from 'common/dao';
import ValidationService from 'common/services/ValidationService';
import {getVehicleDetails} from 'partner/actions/PartnerActions';
import {isEqual} from 'lodash';
import shotgun from 'native-base-theme/variables/shotgun';

class PartnerVehicleDetails extends Component{
  componentWillMount() {
    const {vehicleDetails} = this.props;

    if (vehicleDetails !== undefined) {
      this.setState({vehicle: vehicleDetails});
    }
  }

  componentWillReceiveProps(newProps){
    const {vehicleDetails} = newProps;
    const {vehicle, vehicleDetails: oldVehicleDetails} = this.props;

    if (!isEqual(vehicleDetails, oldVehicleDetails) || vehicle == undefined){
      this.setState({vehicle: {...vehicleDetails}, errors: ''});
    }
  }

  setRegistrationNumber = (registrationNumber) => {
    this.setState({registrationNumber});
  }

  toggleProduct = (productId) => {
    let {selectedProductIds = []} = this.props;
    const index = selectedProductIds.indexOf(productId);
    if (!!~index){
      selectedProductIds = selectedProductIds.filter((_, idx) => idx !== index);
    } else {
      selectedProductIds = [...selectedProductIds, productId];
    }
    this.setState({selectedProductIds});
  }

  requestVehicleDetails = () => {
    const {dispatch, registrationNumber} = this.props;
    dispatch(getVehicleDetails(registrationNumber));
  }

  rowView = ({item, index: i, selectedProductIds}) => {
    const {productId, name} = item;
    const isSelected = !!~selectedProductIds.indexOf(productId);

    return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
      <Button style={{height: 'auto'}} large active={isSelected} onPress={() => this.toggleProduct(productId)}>
        <Icon name={productId}/>
      </Button>
      <Text style={styles.productSelectText}>{name}</Text>
    </View>;
  }

  render(){
    const {vehicleDetails, errors, busy, busyLoadingProducts, registrationNumber, contentType, selectedProductIds = [], options} = this.props;

    return busyLoadingProducts ? <LoadingScreen text="Loading Vehicle Types" /> : <ScrollView>
      <Grid>
        <Row>
          <Col>
            <Item stackedLabel style={styles.vehicleRegItem}>
              <Label>Vehicle registration</Label>
              <ValidatingInput bold placeholder='AB01 CDE' value={registrationNumber} validateOnMount={registrationNumber !== undefined} onChangeText={(value) => this.setRegistrationNumber(value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
            </Item>
            <ValidatingButton fullWidth busy={busy} onPress={this.requestVehicleDetails} validateOnMount={registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
          </Col>
        </Row>

        {vehicleDetails.make != undefined ? (<Row style={{flexWrap: 'wrap'}}>
          <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Registration number</Label>
            <Text>{vehicleDetails.registrationNumber}</Text>
          </Item></Col>
          <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Vehicle model</Label>
            <Text>{`${vehicleDetails.make} ${vehicleDetails.model}`} </Text>
          </Item></Col>
          <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Vehicle colour</Label>
            <Text>{vehicleDetails.colour}</Text>
          </Item></Col>
          <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
            <Label>Approx dimensions</Label>
            <Text>{`${vehicleDetails.volume}m\xB3 load space`}</Text>
            <Text>{`${vehicleDetails.weight}kg Max`}</Text>
          </Item></Col>
        </Row>) : null}
        <ErrorRegion errors={errors}/>
        <Row><Text style={styles.selectText}>Select which vehicle type jobs you'd like to see</Text></Row>
        <PagingListView
          style={styles.pagingListView}
          {...{selectedProductIds}}
          daoName='productDao'
          dataPath={['product', 'products']}
          pageSize={10}
          scrollContainer={View}
          scrollContainerStyle={styles.scrollContainerStyle}
          elementContainer={Row}
          elementContainerStyle={{flexWrap: 'wrap'}}
          options={{...options, categoryId: contentType.rootProductCategory}}
          rowView={this.rowView}
          paginationWaitingView={() => <Spinner />}
          emptyView={() => <Text empty>No items to display</Text>}
          headerView={undefined}
        />
      </Grid>
    </ScrollView>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  volume: yup.number().required(),
  weight: yup.number().required()
};

const styles = {
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
  },
  selectText: {
    marginTop: shotgun.contentPadding,
    fontWeight: 'bold',
    fontSize: 16
  },
  vehicleRegItem: {
    marginLeft: 0,
    borderBottomWidth: 0
  },
  scrollContainerStyle: {
    marginTop: shotgun.contentPadding,
    marginBottom: shotgun.contentPadding,
  },
  productSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    vehicleDetails: getDaoState(state, ['vehicleDetails'], 'partnerDao') || getDaoState(state, ['user', 'vehicle'], 'userDao') || {},
    options: getDaoOptions(state, 'productDao'),
    errors: getOperationError(state, 'partnerDao', 'getVehicleDetails'),
    busy: isAnyOperationPending(state, [{ partnerDao: 'getVehicleDetails'}]),
    busyLoadingProducts: isAnyLoading(state, ['productDao'])
  };
};

const canSubmit = async (state, user) => {
  const {vehicle, selectedProductIds} = state;

  if (user !== undefined) {
    return await ValidationService.validate(vehicle, yup.object(validationSchema));
  }
  return await ValidationService.validate({...vehicle, selectedProductIds}, yup.object(validationSchema, {selectedProductIds: yup.array().required()}));
};

export const ConnectedPartnerVehicleDetails =  withExternalState(mapStateToProps)(PartnerVehicleDetails);
export default {control: ConnectedPartnerVehicleDetails, validator: canSubmit};
