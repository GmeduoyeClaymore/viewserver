import React, {Component} from 'react';
import {
  Image,
  StyleSheet,
  TouchableWithoutFeedback,
} from 'react-native';
import {View, Text} from 'native-base';
import ImageCarousel from 'react-native-image-carousel';
import shotgun from 'native-base-theme/variables/shotgun';


export default class RatingsImages extends Component{
  constructor(props){
    super(props);
    this.state = {
      busy: false
    };
  }
  
    showLightBox = () => {
      this._imageCarousel.open(0);
    }
  
    _renderHeader = () =>{
      return  <TouchableWithoutFeedback onPress={this._imageCarousel.close} style={{padding: 0}}>
        <View>
          <Text style={styles.closeText}>Exit</Text>
        </View>
      </TouchableWithoutFeedback>;
    }
   
    _renderFooter = () =>  {
      return (
        <Text style={styles.footerText}></Text>
      );
    }
   
    _renderContent = (idx) =>{
      const {images: urls = []} = this.props;
      return (
        <Image
          style={StyleSheet.absoluteFill}
          source={{ uri: urls[idx] }}
          resizeMode={'contain'}
        />
      );
    }
  
    render(){
      const {images: urls = []} = this.props;
      return <ImageCarousel
        style={{marginTop: 10, marginBottom: 10, height: 360}}
        ref={car => {this._imageCarousel = car;}}
        renderContent={this._renderContent}
        renderHeader={this._renderHeader}
        renderFooter={this._renderFooter}>
        {[...urls].map(url => (
          <Image
            key={url}
            source={{ uri: url, width: 300 }}
            resizeMode={'contain'}
          />
        ))}
      </ImageCarousel>;
    }
}


const styles = {
  closeText: {
    color: 'white',
    textAlign: 'right',
    padding: 15,
  },
  footerText: {
    color: 'white',
    textAlign: 'center',
  },
  image: {
    marginRight: 2,
    height: 300,
    marginBottom: 10,
  },
  noJobs: {
    margin: shotgun.contentPadding
  },
  title: {
    alignSelf: 'flex-start'
  },
  time: {
    flexDirection: 'row',
    paddingTop: 5
  },
  comments: {
    alignSelf: 'flex-start',
    color: shotgun.brandLight,
    fontStyle: 'italic',
    paddingTop: 10
  }
};
  
