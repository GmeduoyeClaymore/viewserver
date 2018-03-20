import React, {Component} from 'react';
import {H1, Button, Container, Text, Grid, Row, Content, View} from 'native-base';
import { Image} from 'react-native';
import {INITIAL_STATE} from './CheckoutInitialState';
import yup from 'yup';
import {ValidatingButton, Icon} from 'common/components';
import {resolveContentTypeIcon} from 'common/assets';

class ContentTypeSelect extends Component{
  constructor(props){
    super(props);
    this.startOrder = this.startOrder.bind(this);
    this.selectContentType = this.selectContentType.bind(this);
  }

  selectContentType(selectedContentType){
    const {context} = this.props;
    const orderItem = {...INITIAL_STATE.orderItem, contentTypeId: selectedContentType.contentTypeId};
    context.setState({...INITIAL_STATE, selectedContentType, orderItem});
  }

  startOrder(){
    const {context, navigationStrategy} = this.props;
    const {selectedContentType} = context.state;
    navigationStrategy.init(selectedContentType.contentTypeId);
    navigationStrategy.next();
  }

  render(){
    const {contentTypes = [], context = {}} = this.props;
    const {selectedContentType} = context.state;
    return (
      <Container>
        <Content padded>
          <View>
            <H1 style={styles.h1}>Start a new job</H1>
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
  subTitle: {
    marginTop: 25,
    marginBottom: 30,
    fontSize: 13
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
    marginBottom: 30
  },
  wrapper: {
    height: 600,
    justifyContent: 'center'
  },
  picture: {
    width: 90,
    height: 90,
    borderWidth: 0
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center'
  }
};

export default ContentTypeSelect;
