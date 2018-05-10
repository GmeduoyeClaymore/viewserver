import React, {Component} from 'react';
import {H1, Button, Container, Text, Grid, Row, Content, View} from 'native-base';
import {DELIVERY_ORDER_INITIAL_STATE, RUBBISH_ORDER_INITIAL_STATE, PERSONELL_ORDER_INITIAL_STATE} from './CheckoutInitialState';
import yup from 'yup';
import {ValidatingButton, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {getDaoState} from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';

class ContentTypeSelect extends Component{
  beforeNavigateTo(){
    this.setState({});
  }
  
  selectContentType = (selectedContentType) => {
    const resources = resourceDictionary.resolve(selectedContentType.contentTypeId);
    const {InitialState} = resources;
    this.setState({...InitialState, selectedContentType, selectedCategory: selectedContentType.productCategory});
  }

  startOrder = () => {
    const {history, next, user, ordersRoot, parentPath} = this.props;
    if (user.paymentCards.length > 0) {
      history.push(next);
    } else {
      history.push({pathname: `${ordersRoot}/Settings/UpdatePaymentCardDetails`, transition: 'left'}, {next: `${parentPath}`});
    }
  }

  render(){
    const {contentTypes = [], selectedContentType = {}} = this.props;
    return (
      <Container>
        <Content padded>
          <View style={styles.title}>
            <H1 style={styles.h1}>Create a job</H1>
            <Text  style={styles.subTitle} subTitle>What kind of service do you need?</Text>
          </View>
          <Grid>
            <Row style={{flexWrap: 'wrap'}}>
              {contentTypes.map((v, i) => {
                return <View key={i} style={{width: '50%', paddingRight: 5, paddingLeft: 5, maxWidth: 250, maxHeight: 250}}>
                  <Button style={{height: 'auto'}} large active={selectedContentType.contentTypeId == v.contentTypeId} onPress={() => this.selectContentType(v)}>
                    <Icon name={`content-type-${v.contentTypeId}`}/>
                  </Button>
                  <Text style={styles.contentTypeSelectText}>{v.name}</Text>
                </View>;
              })}
            </Row>
          </Grid>
          <Text note style={{marginTop: 20}}>{selectedContentType !== undefined ? selectedContentType.description : null}</Text>
        </Content>
        <ValidatingButton fullWidth paddedBottomLeftRight iconRight onPress={this.startOrder}
          validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedContentType}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </Container>
    );
  }
}

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('InitialState', {}).
    delivery(DELIVERY_ORDER_INITIAL_STATE).
    rubbish(RUBBISH_ORDER_INITIAL_STATE).
    personell(PERSONELL_ORDER_INITIAL_STATE)
/*eslint-enable */

const validationSchema = {
  contentTypeId: yup.string().required()
};

const styles = {
  title: {
    marginTop: 25
  },
  subTitle: {
    marginTop: 10,
    marginBottom: 20,
  },
  contentTypeSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  },
  h1: {
    justifyContent: 'center',
    marginBottom: 0
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    user: getDaoState(state, ['user'], 'userDao')
  };
};

export default withExternalState(mapStateToProps)(ContentTypeSelect);
