import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import { getDaoState, isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError} from 'common/dao';
import ReactNativeModal from 'react-native-modal';
import ContentTypeSelector from 'driver/registration/ContentTypeSelector';
import {updateDriver, updateVehicle} from 'driver/actions/DriverActions';
import Immutable from 'seamless-immutable';
import * as ContentTypes from 'common/constants/ContentTypes';

class ConfigureServices extends Component{
  updateServices = async() => {
    //TODO - this selectedcontenttypes thing is a horrible mess, a big ball of json urg, why is it an object and not array as well?
    const {user, selectedContentTypes, dispatch, history, parentPath} = this.props;
    const vehicle = selectedContentTypes[ContentTypes.DELIVERY] ? selectedContentTypes[ContentTypes.DELIVERY].vehicle : undefined;
    const filteredContentTypes = {};

    for (const [key, value] of Object.entries(selectedContentTypes)) {
      const {selectedProductCategories, selectedProductIds} = value;
      filteredContentTypes[key] = {selectedProductCategories, selectedProductIds};
    }

    const persistedUser =  {...user, selectedContentTypes: JSON.stringify(filteredContentTypes)};

    if (vehicle){
      dispatch(updateVehicle(vehicle));
    }

    dispatch(updateDriver(persistedUser, () => history.push(`${parentPath}/DriverSettingsLanding`)));
  }

  render(){
    const {history, contentTypes = [], selectedContentTypes = {}, errors, busy} = this.props;
    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Configure Services</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always" padded>
        <Grid>
          <Row>
            <Col>
              <View style={{...styles.productSelectView}}>
                <Grid>
                  <Row style={{flexWrap: 'wrap'}}>
                    {contentTypes.map((contentType, i) =>
                      <View key={i} style={{width: '50%', padding: 10}}>
                        <ContentTypeSelector {...{...this.props, contentType, selected: !!selectedContentTypes[contentType.contentTypeId]}}/></View>)}
                  </Row>
                </Grid>
              </View>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy} onPress={this.updateServices} validationSchema={yup.object(validationSchema)} model={{selectedContentTypes: Object.keys(selectedContentTypes)}}>
        <Text uppercase={false}>Save</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

const styles = {
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
  selectedContentTypes: yup.array().required()
};


const mapStateToProps = (state, initialProps) => {
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const registrationErrors = getOperationError(state, 'driverDao', 'updateDriver') || [];
  const busy = isAnyOperationPending(state, [{ driverDao: 'updateDriver'}]) ||  isAnyLoading(state, ['contentTypeDao', 'driverDao']);
  const user =  getDaoState(state, ['user'], 'userDao');
  const selectedContentTypes = Immutable(JSON.parse(user.selectedContentTypes));
  return {
    ...initialProps,
    user,
    selectedContentTypes: initialProps.selectedContentTypes || selectedContentTypes,
    contentTypes,
    busy,
    errors: [loadingErrors, registrationErrors].filter( c=> !!c).join('\n')
  };
};


export default withExternalState(mapStateToProps)(ConfigureServices);
