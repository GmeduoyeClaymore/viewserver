import React, { Component } from 'react';
import { View } from 'react-native';
import { Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col } from 'native-base';
import yup from 'yup';
import { ValidatingButton, ErrorRegion, Icon } from 'common/components';
import { withExternalState } from 'custom-redux';
import { getDaoState, isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError } from 'common/dao';
import ReactNativeModal from 'react-native-modal';
import ContentTypeSelector from 'partner/registration/ContentTypeSelector';
import { updatePartner, updateVehicle } from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';

class ConfigureServices extends Component {
  updateServices = async () => {
    //TODO - this selectedcontenttypes thing is a horrible mess, a big ball of json urg, why is it an object and not array as well?
    const { user, selectedContentTypes, dispatch, history, parentPath } = this.props;
    const vehicle = selectedContentTypes[ContentTypes.DELIVERY] ? selectedContentTypes[ContentTypes.DELIVERY].vehicle : user.vehicle;
    const filteredContentTypes = {};

    for (const [key, value] of Object.entries(selectedContentTypes)) {
      const { selectedProductCategories, selectedProductIds } = value;
      filteredContentTypes[key] = { selectedProductCategories, selectedProductIds };
    }

    const persistedUser = { ...user, selectedContentTypes: filteredContentTypes, vehicle};

    dispatch(updatePartner(persistedUser, () => history.push(`${parentPath}/PartnerSettingsLanding`)));
  }

  render() {
    const { history, contentTypes = [], selectedContentTypes = {}, errors, busy } = this.props;
    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow' />
          </Button>
        </Left>
        <Body><Title>Configure Services</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always" padded>
        <Grid>
          <Row>
            <Col>
              <View style={{ ...styles.productSelectView }}>
                <Grid>
                  <Row style={{ flexWrap: 'wrap' }}>
                    {contentTypes.map((contentType, i) =>
                      <View key={i} style={{ width: '50%', padding: 10 }}>
                        <ContentTypeSelector {...{ ...this.props, contentType, selected: !!selectedContentTypes[contentType.contentTypeId] }} /></View>)}
                  </Row>
                </Grid>
              </View>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors} />
      <ValidatingButton paddedBottomLeftRight fullWidth iconRight validateOnMount={true} busy={busy} onPress={this.updateServices} validationSchema={yup.object(validationSchema)} model={{ selectedContentTypes: Object.keys(selectedContentTypes) }}>
        <Text uppercase={false}>Save</Text>
        <Icon next name='forward-arrow' />
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
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const registrationErrors = getOperationError(state, 'partnerDao', 'updatePartner') || [];
  const user = getDaoState(state, ['user'], 'userDao');

  return {
    ...initialProps,
    user,
    selectedContentTypes: initialProps.selectedContentTypes || user.selectedContentTypes,
    contentTypes: getDaoState(state, ['contentTypes'], 'contentTypeDao'),
    busy: isAnyOperationPending(state, [{ partnerDao: 'updatePartner' }]) || isAnyLoading(state, ['contentTypeDao', 'partnerDao']),
    errors: [loadingErrors, registrationErrors].filter(c => !!c).join('\n')
  };
};


export default withExternalState(mapStateToProps)(ConfigureServices);
