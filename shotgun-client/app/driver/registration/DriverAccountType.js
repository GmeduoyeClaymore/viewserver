import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {withRouter} from 'react-router';
import { getDaoState, isAnyLoading, getLoadingErrors} from 'common/dao';

class DriverAccountType extends Component{
  constructor(props){
    super(props);
    this.selectContentType = this.selectContentType.bind(this);
    this.renderContentType = this.renderContentType.bind(this);
  }

  selectContentType(selectedContentType){
    let {selectedContentTypes = []} = this.props;
    const {context} = this.props;
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
        <Icon name='small-van'/>
      </Button>
      <Text style={styles.productSelectTextRow}>{contentType.name}</Text>
    </View>;
  }

  render(){
    const {history, contentTypes = [], selectedContentTypes = [], errors, busy} = this.props;

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
                    {contentTypes.map((v, i) => this.renderContentType(v, i))}
                  </Row>
                </Grid>
              </View>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy} onPress={() => history.push('/Driver/Registration/DriverCapabilityDetails')} validationSchema={yup.object(validationSchema)} model={{selectedContentTypes}}>
        <Text uppercase={false}>Continue</Text>
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
  const {context = {}} = initialProps;
  const {errors = [], selectedContentTypes} = context.state;
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy = isAnyLoading(state, ['contentTypeDao', 'driverDao']);
  return {
    ...initialProps,
    context,
    selectedContentTypes,
    contentTypes,
    busy,
    errors: [loadingErrors, errors].filter( c=> !!c).join('\n')
  };
};


export default withRouter(connect(mapStateToProps)(DriverAccountType));
