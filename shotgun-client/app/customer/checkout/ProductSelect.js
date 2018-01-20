import React, {Component} from 'react';
import {Text, Content, Button, H1, Grid, Row, Icon, Col, View} from 'native-base';
import {merge} from 'lodash';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';

class ProductSelect extends Component{
  constructor(props){
    super(props);
  }

  renderContentType(contentType, i){
    const {context} = this.props;
    const {selectedContentType = {}} = context.state;
    return <View key={i} style={{width: '30%'}}>
      <Button style={{height: 'auto'}} large active={contentType.contentTypeId == selectedContentType.contentTypeId} onPress={() => this.selectContentType(contentType)}>
        <Icon name='car'/>
      </Button>
      <Text style={styles.productSelectTextRow}>{contentType.name}</Text>
    </View>;
  }

  selectContentType(selectedContentType){
    const {context, navigationStrategy, history} = this.props;
    context.setState({selectedContentType});
    navigationStrategy.init(selectedContentType.contentTypeId, history);
    navigationStrategy.next();
  }

  render(){
    const {contentTypes} = this.props;
    return (
      <Content padded contentContainerStyle={styles.container}>
        <View style={styles.titleView}>
          <H1 style={styles.h1}>Start a new job</H1>
          <Text subTitle>What kind of service do you need?</Text>
        </View>
        <View style={styles.productSelectView}>

          <Grid>
            <Row style={{flexWrap: 'wrap'}}>
              {contentTypes.map((v, i) => this.renderContentType(v, i))}
            </Row>
          </Grid>
        </View>
      </Content>
    );
  }
}

const styles = {
  h1: {
    width: '80%',
    marginBottom: 30
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center'
  },
  titleView: {
    flex: 1,
    justifyContent: 'flex-end'
  },
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

export default ProductSelect;
