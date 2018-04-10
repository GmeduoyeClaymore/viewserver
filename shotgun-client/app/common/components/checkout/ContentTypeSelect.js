import React, {Component} from 'react';
import {H1, Button, Container, Text, Grid, Row, Content, View} from 'native-base';
import {INITIAL_STATE} from './CheckoutInitialState';
import yup from 'yup';
import {ValidatingButton, Icon} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
import {withExternalState} from 'custom-redux';

const withFixedPice = (state) => {
  const delivery = {...state.delivery, isFixedPrice: true};
  return {...state, delivery};
};

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('InitialState', INITIAL_STATE).
    personell(withFixedPice(INITIAL_STATE))
/*eslint-enable */

class ContentTypeSelect extends Component{
  constructor(props){
    super(props);
    this.startOrder = this.startOrder.bind(this);
    this.selectContentType = this.selectContentType.bind(this);
  }
  
  beforeNavigateTo(){
    const {resetParentComponentState} = this.props;
    resetParentComponentState();
  }
  
  selectContentType(selectedContentType){
    const resources = resourceDictionary.resolve(selectedContentType.contentTypeId);
    const initialState = resources.InitialState;
    const orderItem = {...initialState.orderItem, contentTypeId: selectedContentType.contentTypeId};
    const {resetParentComponentState} = this.props;
    resetParentComponentState(() => this.setState({...initialState, selectedContentType, orderItem, selectedCategory: selectedContentType.productCategory}));
  }

  startOrder(){
    const {history, next} = this.props;
    history.push(next);
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
                return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
                  <Button style={{height: 'auto'}} large active={selectedContentType.contentTypeId == v.contentTypeId} onPress={() => this.selectContentType(v)}>
                    <Icon name={v.imageUrl || 'dashed'}/>
                  </Button>
                  <Text style={styles.contentTypeSelectText}>{v.name}</Text>
                </View>;
              })}
            </Row>
          </Grid>
          <Text note style={{marginTop: 20}}>{selectedContentType !== undefined ? selectedContentType.description : null}</Text>
        </Content>
        <ValidatingButton fullWidth paddedBottom iconRight onPress={() => this.startOrder()}
          validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedContentType}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </Container>
    );
  }
}

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

export default withExternalState()(ContentTypeSelect);
