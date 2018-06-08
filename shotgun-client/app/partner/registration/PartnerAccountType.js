import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ErrorRegion, Icon, TermsAgreement} from 'common/components';
import {withExternalState} from 'custom-redux';
import {getDaoState, isAnyLoading, getLoadingErrors} from 'common/dao';
import ReactNativeModal from 'react-native-modal';
import ContentTypeSelector from './ContentTypeSelector';

class PartnerAccountType extends Component{
  constructor(props){
    super(props);
  }


  render(){
    const {history, contentTypes = [], selectedContentTypes = {}, errors, busy, nextAction} = this.props;
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
        <ErrorRegion errors={errors}/>
        <Grid>
          <Row>
            <Col>
              <View style={styles.productSelectView}>
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
      <ValidatingButton paddedBottomLeftRight fullWidth iconRight arrow={true} validateOnMount={true} busy={busy} onPress={nextAction.bind(this)}  validationSchema={yup.object(validationSchema)} model={{selectedContentTypes: Object.keys(selectedContentTypes)}}>
        <Text uppercase={false}>Register</Text>
      </ValidatingButton>
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
  const {errors = [], selectedContentTypes, busyUpdating} = initialProps;
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy = busyUpdating ||  isAnyLoading(state, ['contentTypeDao', 'partnerDao']);
  return {
    ...initialProps,
    selectedContentTypes,
    contentTypes,
    busy,
    errors: [loadingErrors, errors].filter( c=> !!c).join('\n')
  };
};


export default withExternalState(mapStateToProps)(PartnerAccountType);
