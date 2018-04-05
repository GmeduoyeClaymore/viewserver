import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ErrorRegion, Icon, TermsAgreement} from 'common/components';
import {withExternalState} from 'custom-redux';
import { getDaoState, isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError} from 'common/dao';
import ReactNativeModal from 'react-native-modal';
import ContentTypeSelector from './ContentTypeSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import {registerAndLoginDriver} from 'common/actions/CommonActions';

class DriverAccountType extends Component{
  constructor(props){
    super(props);
    this.register = this.register.bind(this);
  }

  async register(){
    const {user, bankAccount, address, selectedContentTypes, dispatch, history} = this.props;
    const persistedUser = user.setIn(['selectedContentTypes'], JSON.stringify(selectedContentTypes));
    const vehicle = selectedContentTypes[ContentTypes.DELIVERY] ? selectedContentTypes[ContentTypes.DELIVERY].vehicle : {};
    dispatch(registerAndLoginDriver(persistedUser, vehicle, address, bankAccount, () => history.push('/Root')));
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
        <Body><Title>Account Type</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always" padded>
        <Grid>
          <Row>
            <Col>
              <ErrorRegion errors={errors}/>
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
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy} onPress={this.register} validationSchema={yup.object(validationSchema)} model={{selectedContentTypes: Object.keys(selectedContentTypes)}}>
          <Text uppercase={false}>Register</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </ErrorRegion>
      <TermsAgreement history={history}/>
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
  selectedContentTypes: yup.array().min(0).required()
};

const mapStateToProps = (state, initialProps) => {
  const {errors = [], selectedContentTypes} = initialProps;
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const registrationErrors = getOperationError(state, 'loginDao', 'registerAndLoginDriver') || [];
  const busy = isAnyOperationPending(state, [{ loginDao: 'registerAndLoginDriver'}]) ||  isAnyLoading(state, ['contentTypeDao', 'driverDao']);

  return {
    ...initialProps,
    selectedContentTypes,
    contentTypes,
    busy,
    errors: [loadingErrors, errors, registrationErrors].filter( c=> !!c).join('\n')
  };
};


export default withExternalState(mapStateToProps)(DriverAccountType);
