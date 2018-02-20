import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {withRouter} from 'react-router';
import { getDaoState, isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError} from 'common/dao';
import ReactNativeModal from 'react-native-modal';
import ContentTypeSelector from './ContentTypeSelector';
import * as ContentTypes from 'common/constants/ContentTypes';
import {registerDriver} from 'driver/actions/DriverActions';

class DriverAccountType extends Component{
  constructor(props){
    super(props);
    this.register = this.register.bind(this);
  }

  async register(){
    const {user, bankAccount, address, selectedContentTypes, dispatch, history} = this.props;
    user.selectedContentTypes = JSON.stringify(selectedContentTypes);
    const vehicle = selectedContentTypes[ContentTypes.DELIVERY] ? selectedContentTypes[ContentTypes.DELIVERY].vehicle : {};
    dispatch(registerDriver(user, vehicle, address, bankAccount, () => history.push('/Root')));
  }


  render(){
    const {history, contentTypes = [], selectedContentTypes = {}, errors, busy, context} = this.props;
    const {state} = this;
    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => history.goBack()}/>
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
                        <ContentTypeSelector {...{...this.props, ...state, context, contentType, selected: !!selectedContentTypes[contentType.contentTypeId]}}/></View>)}
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
  const {context = {}} = initialProps;
  const {errors = [], selectedContentTypes} = context.state;
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const registrationErrors = getOperationError(state, 'driverDao', 'registerDriver') || [];
  const busy = isAnyOperationPending(state, [{ driverDao: 'registerDriver'}]) ||  isAnyLoading(state, ['contentTypeDao', 'driverDao']);

  return {
    ...initialProps,
    context,
    ...context.state,
    selectedContentTypes,
    contentTypes,
    busy,
    errors: [loadingErrors, errors, registrationErrors].filter( c=> !!c).join('\n')
  };
};


export default withRouter(connect(mapStateToProps)(DriverAccountType));
