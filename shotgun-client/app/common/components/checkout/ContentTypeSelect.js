import React, {Component} from 'react';
import {Image} from 'react-native';
import {H1, Button, Container, Text, Grid, Row, Content, View} from 'native-base';
import {DELIVERY_ORDER_INITIAL_STATE} from './CheckoutInitialState';
import yup from 'yup';
import {ValidatingButton, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {ContentTypeImages} from 'common/assets/img/Images';
import * as ContentTypes from 'common/constants/ContentTypes';

class ContentTypeSelect extends Component{
  beforeNavigateTo(){
    this.setState({});
  }
  
  selectContentType = (selectedContentType) => {
    const resources = resourceDictionary.resolve(this.selectContentType.contentTypeId);
    const {InitialState} = resources;
    this.setState({...InitialState, selectedContentType, selectedCategory: selectedContentType.productCategory});
  }

  startOrder = () => {
    const {history, next, paymentCards, ordersRoot, parentPath} = this.props;

    if (paymentCards.length > 0) {
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
                    <Image source={ContentTypeImages[v.contentTypeId]} style={styles.image}/>
                  </Button>
                  <Text style={styles.contentTypeSelectText}>{v.name}</Text>
                </View>;
              })}
            </Row>
          </Grid>
          <Text note style={{marginTop: 20}}>{selectedContentType !== undefined ? selectedContentType.description : null}</Text>
        </Content>
        <ValidatingButton fullWidth paddedBottom iconRight onPress={this.startOrder}
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
    delivery(DELIVERY_ORDER_INITIAL_STATE)
/*eslint-enable */

const validationSchema = {
  contentTypeId: yup.string().required()
};

const styles = {
  title: {
    marginTop: 25
  },
  image: {
    resizeMode: 'contain',
    height: '70%',
    width: '100%',
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
    paymentCards: getDaoState(state, ['paymentCards'], 'paymentDao') || []
  };
};


export default withExternalState(mapStateToProps)(ContentTypeSelect);
