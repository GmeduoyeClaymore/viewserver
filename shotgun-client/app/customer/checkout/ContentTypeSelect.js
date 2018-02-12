import React, {Component} from 'react';
import {Text, Content, Button, H1, Grid, Row, View} from 'native-base';
import {merge} from 'lodash';
import {Icon} from 'common/components';
import { Image, TouchableOpacity} from 'react-native';
import Swiper from 'react-native-swiper';

import {resolveContentTypeIcon} from 'common/assets';

class ContentTypeSelect extends Component{
  constructor(props){
    super(props);
  }

  renderContentType(contentType, i){
    const {context} = this.props;
    const {selectedContentType = {}} = context.state;
    return <View key={i} style={{flex: 1, justifyContent: 'center'}}>
      <TouchableOpacity style={{height: 'auto', borderWidth: 0, justifyContent: 'center', paddingLeft: 25, paddingRight: 30,  flex: 4}} large active={contentType.contentTypeId == selectedContentType.contentTypeId} onPress={() => this.selectContentType(contentType)}>
        <Image resizeMode="contain" source={resolveContentTypeIcon(contentType)}  style={styles.picture}/>
      </TouchableOpacity>
      <View style={{flex: 2}}>
        <Text style={styles.contentTypeSelectTextRow}>{contentType.name}</Text>
        <Text style={styles.contentTypeSelectTextRowSummary}>{contentType.description}</Text>
      </View>
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
          <Text  style={styles.subTitle} subTitle>What kind of service do you need?</Text>
        </View>
        <View style={styles.contentTypeSelectView}>
          <Swiper style={styles.wrapper} showsButtons={true}>
            {contentTypes.map((v, i) => this.renderContentType(v, i))}
          </Swiper>
        </View>
      </Content>
    );
  }
}

const styles = {
  h1: {
    justifyContent: 'center',
    marginBottom: 30
  },
  subTitle: {
    justifyContent: 'center',
    textAlign: 'center'
  },
  wrapper: {
    height: 600,
    justifyContent: 'center'
  },
  picture: {
    width: 300,
    height: 280,
    borderWidth: 0
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
    flex: 3,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  contentTypeSelectTextRow: {
    justifyContent: 'center',
    fontSize: 14,
    fontWeight: 'bold',
    textAlign: 'center',
    paddingTop: 10,
    paddingBottom: 10
  },
  contentTypeSelectTextRowSummary: {
    justifyContent: 'center',
    fontSize: 10,
    paddingTop: 10,
    textAlign: 'center'
  },
  contentTypeSelectText: {
    
  }
};

export default ContentTypeSelect;
