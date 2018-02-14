import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {resolveContentTypeIconSml} from 'common/assets';
import resolveDetailsControl from './ContentTypeDetailRegistry';
import {Image, StyleSheet, Text, TouchableHighlight, TouchableOpacity, Dimensions, View } from 'react-native';
import {Button, Col} from 'native-base';
import ReactNativeModal from 'react-native-modal';
const {width, height} = Dimensions.get('window');
const BORDER_RADIUS = 13;
const BACKGROUND_COLOR = 'white';
const BORDER_COLOR = '#d5d5d5';
const TITLE_FONT_SIZE = 13;
const TITLE_COLOR = '#8f8f8f';
const BUTTON_FONT_WEIGHT = 'normal';
const BUTTON_FONT_COLOR = '#007ff9';
const BUTTON_FONT_SIZE = 20;

const styles = {
  picture: {
    width: 70,
    height: 70,
    borderWidth: 0,
    marginLeft: 15
  },
  contentTypeSelectTextRow: {
    justifyContent: 'center',
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
    paddingTop: 10,
    paddingBottom: 10
  },
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
        
  contentTypeSelectTextRowSummary: {
    justifyContent: 'center',
    fontSize: 10,
    paddingTop: 10,
    fontWeight: 'bold',
    textAlign: 'center'
  },
  contentTypeSelectText: {
          
  },
  
  contentContainer: {
    justifyContent: 'flex-end',
    margin: 10,
  },
  contentTypeSelectorContainer: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    width: width - 20,
    height: 600,
    marginBottom: 8,
    overflow: 'hidden',
  },
  titleContainer: {
    borderBottomColor: BORDER_COLOR,
    borderBottomWidth: StyleSheet.hairlineWidth,
    padding: 14,
    backgroundColor: 'transparent',
  },
  title: {
    textAlign: 'center',
    color: TITLE_COLOR,
    fontSize: TITLE_FONT_SIZE,
  },
  
  confirmButton: {
    borderColor: BORDER_COLOR,
    borderTopWidth: StyleSheet.hairlineWidth,
    backgroundColor: 'transparent',
    height: 57,
    justifyContent: 'center',
  },
  confirmText: {
    textAlign: 'center',
    color: BUTTON_FONT_COLOR,
    fontSize: BUTTON_FONT_SIZE,
    fontWeight: BUTTON_FONT_WEIGHT,
    backgroundColor: 'transparent',
  },
  cancelButton: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    height: 57,
    justifyContent: 'center',
  },
  cancelText: {
    padding: 10,
    textAlign: 'center',
    color: BUTTON_FONT_COLOR,
    fontSize: BUTTON_FONT_SIZE,
    fontWeight: '600',
    backgroundColor: 'transparent',
  }
};

const  confirmButton = <Text style={[styles.confirmText]}>Confirm</Text>;
const cancelButton = <Text style={[styles.cancelText]}>Cancel</Text>;

const TitleContainer = ({title}) => (
  <View style={styles.titleContainer}>
    <Text style={[styles.title]}>{title}</Text>
  </View>
);

export default class ContentTypeSelector extends Component{
    static propTypes = {
      selected: PropTypes.bool,
      contentType: PropTypes.object
    }
    constructor(props){
      super(props);
      this.state = {
        detailVisible: false
      };
      this._handleSelectContentType = this._handleSelectContentType.bind(this);
      this._handleOnModalHide = this._handleOnModalHide.bind(this);
      this._handleConfirm = this._handleConfirm.bind(this);
      this._handleCancel = this._handleCancel.bind(this);
    }

    _handleSelectContentType(){
      const {onContentTypeSelected, selected} = this.props;
      if (onContentTypeSelected){
        onContentTypeSelected(!selected);
      }
      this._handleToggleDetailVisibility(true);
    }

    _handleToggleDetailVisibility(detailVisible){
      this.setState({detailVisible});
    }

    _handleOnModalHide(){
        
    }

    _handleCancel(){
        
    }

    _handleConfirm(){
      const {state} = this;
      const {context} = this.props;
      if (context){
        context.setState({...state});
      }
    }

    render(){
      const {selected, contentType = {}} = this.props;
      const {detailVisible} = this.state;
      const ContentTypeDetailControl = resolveDetailsControl(contentType);
      return <View style={{flex: 1, justifyContent: 'center'}}>
        <Button style={{height: 'auto', borderWidth: 0, justifyContent: 'center', paddingLeft: 25, paddingRight: 30,  flex: 4}} large active={selected} onPress={this._handleSelectContentType}>
          <Col>
            <Image resizeMode="contain" source={resolveContentTypeIconSml(contentType)}  style={styles.picture}/>
            <Text style={styles.contentTypeSelectTextRowSummary}>{contentType.name}</Text>
          </Col>
        </Button>
        
        <ReactNativeModal
          isVisible={detailVisible}
          style={[styles.contentContainer]}
          onModalHide={this._handleOnModalHide}
          backdropOpacity={0.4}
        >
          <View style={[styles.contentTypeSelectorContainer]}>
            <View>
              <TitleContainer title={contentType.name}/>
              <Text style={styles.contentTypeSelectTextRowSummary}>{contentType.description}</Text>
            </View>
            <ContentTypeDetailControl {...{contentType, context: this, ...this.props}}/>
            <TouchableHighlight
              style={styles.confirmButton}
              underlayColor="#ebebeb"
              onPress={this._handleConfirm}>
              {confirmButton}
            </TouchableHighlight>
          </View>

          <TouchableHighlight
            style={styles.cancelButton}
            underlayColor="#ebebeb"
            onPress={this._handleCancel}>
            {cancelButton}
          </TouchableHighlight>
        </ReactNativeModal>
      </View>;
    }
}


