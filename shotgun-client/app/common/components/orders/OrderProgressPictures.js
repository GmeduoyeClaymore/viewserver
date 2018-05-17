import React, {Component} from 'react';
import {
  Image,
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import {Col, Button} from 'native-base';
import {SpinnerButton, Icon, ImageSelector, ErrorRegion} from 'common/components';
import ImageCarousel from 'react-native-image-carousel';


export default class OrderProgressPictures extends Component{
  constructor(props){
    super(props);
    this.state = {
      busy: false
    };
  }

  onSelectImage = async (response) => {
    const {client, orderId} = this.props;
    const {data: imageData} = response;
    try {
      this.setState({busy: true});
      await client.invokeJSONCommand('personellOrderController', 'addOrderImage', {orderId, imageData});
      this.setState({busy: false});
    } catch (errors){
      this.setState({errors, busy: false});
    }
  }

  deleteImage = async(imageUrl) => {
    try {
      const {client, orderId} = this.props;
      this.setState({busy: true});
      await client.invokeJSONCommand('personellOrderController', 'deleteImage', {orderId, imageUrl});
      this.setState({busy: false});
    } catch (errors){
      this.setState({errors: errors + '', busy: false});
    }
  }

  showPicker = () => {
    const {onSelectImage} = this;
    ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: {}});
  }

  showLightBox = () => {
    this._imageCarousel.open();
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
        cache='force-cache'
        style={StyleSheet.absoluteFill}
        source={{ uri: urls[idx], cache: 'force-cache' }}
        resizeMode={'contain'}
      />
    );
  }

  render(){
    const {images: urls = []} = this.props;
    const {showPicker, deleteImage} = this;
    const {busy, errors} = this.state;
    
    return <Col style={{height: 150}}>
      <ErrorRegion errors={errors}/>
      {urls.length ? <ImageCarousel
        style={{marginTop: 10, marginBottom: 10, height: 360}}
        ref={car => {this._imageCarousel = car;}}
        renderContent={this._renderContent}
        renderHeader={this._renderHeader}
        renderFooter={this._renderFooter}>
        {[...urls].map(url => (
          <Col
            style={styles.imageContainer}>
            <Image
              key={url}
              
              style={styles.image}
              source={{ uri: url, width: 300, cache: 'force-cache' }}
              resizeMode={'contain'}
            />
            <Button fullWidth danger busy={busy} style={{marginLeft: 35, marginRight: 35}} onPress={() => deleteImage(url)}>
              <Text>Delete Image</Text>
            </Button>
          </Col>
        ))}
      </ImageCarousel> : null}
      <SpinnerButton busy={busy} style={!urls.length ? styles.imageButtonLarge : styles.imageButtonSml } photoButton onPress={showPicker}>
        <Icon name='camera' style={{marginBottom: 15}}/>
      </SpinnerButton>
    </Col>;
  }
}

const styles = {
  imageButtonSml: {
    height: 80
  },
  imageButtonLarge: {
    height: 150
  },
  imageButtonIconRow: {
    height: 180,
    justifyContent: 'flex-start',
    alignItems: 'flex-end'
  },
  container: {
    flex: 1,
    justifyContent: 'center',
  },
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
  imageContainer: {
    width: 300,
    flex: 1,
  },
};

