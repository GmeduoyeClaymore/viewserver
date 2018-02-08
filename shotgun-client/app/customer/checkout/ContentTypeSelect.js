import React, {Component} from 'react';
import {Text, Content, Button, H1, Grid, Row, View} from 'native-base';
import {merge} from 'lodash';
import {Icon} from 'common/components';

class ContentTypeSelect extends Component{
  constructor(props){
    super(props);
  }

  renderContentType(contentType, i){
    const {context} = this.props;
    const {selectedContentType = {}} = context.state;
    return <View key={i} style={{width: '30%'}}>
      <Button style={{height: 'auto'}} large active={contentType.contentTypeId == selectedContentType.contentTypeId} onPress={() => this.selectContentType(contentType)}>
        <Icon name='small-van'/>
      </Button>
      <Text style={styles.contentTypeSelectTextRow}>{contentType.name}</Text>
    </View>;
  }

  selectContentType(selectedContentType){
    const {context, navigationStrategy} = this.props;
    let {orderItem} = context.state;
    orderItem = merge({}, orderItem, {contentTypeId: selectedContentType.contentTypeId});
    context.setState({selectedContentType, orderItem});
    navigationStrategy.init(selectedContentType.contentTypeId);
    navigationStrategy.next();
  }

  render(){
    const {contentTypes = []} = this.props;
    return (
      <Content padded contentContainerStyle={styles.container}>
        <View style={styles.titleView}>
          <H1 style={styles.h1}>Start a new job</H1>
          <Text subTitle>What kind of service do you need?</Text>
        </View>
        <View style={styles.contentTypeSelectView}>

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
  contentTypeSelectView: {
    flex: 2,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  contentTypeSelectTextRow: {
    justifyContent: 'center'
  },
  contentTypeSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    width: '80%',
    textAlign: 'center'
  }
};

export default ContentTypeSelect;
