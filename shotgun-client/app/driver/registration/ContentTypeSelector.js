import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {resolveContentTypeIconSml} from 'common/assets';
import resolveDetailsControl from './ContentTypeDetailRegistry';
import {Image, StyleSheet, Text, TouchableHighlight, Dimensions, View } from 'react-native';
import {Button, Col} from 'native-base';
import ReactNativeModal from 'react-native-modal';
import * as ContentTypes from 'common/constants/ContentTypes';
const {height, width} = Dimensions.get('window');
const BORDER_RADIUS = 13;
const BACKGROUND_COLOR = 'white';
const BORDER_COLOR = '#d5d5d5';
const TITLE_FONT_SIZE = 13;
const TITLE_COLOR = '#8f8f8f';
const BUTTON_FONT_WEIGHT = 'normal';
const BUTTON_FONT_COLOR = '#007ff9';
const BUTTON_FONT_COLOR_DISABLED = '#d6e9fc';
const BUTTON_FONT_SIZE = 20;

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('PageTitle', ({contentType}) => contentType.name).
    delivery(() => 'What vehicle have you got?').
    personell(() => 'What skills do you have?').
    rubbish(() => 'Can you do commercial and household waste?');
/*eslint-enable */

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
    margin: 10,
  },
  contentTypeSelectorContainer: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    width: width - 20,
    height: height - 100,
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
  confirmTextDisabled: {
    textAlign: 'center',
    color: BUTTON_FONT_COLOR_DISABLED,
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

const  confirmButton = (isDisabled) => <Text style={[isDisabled ? styles.confirmTextDisabled : styles.confirmText]}>Confirm</Text>;
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
        detailVisible: false,
        canSubmit: false,
        showErrors: false,
        content: {}
      };
      this._handleSelectContentType = this._handleSelectContentType.bind(this);
      this._handleOnModalHide = this._handleOnModalHide.bind(this);
      this._handleConfirm = this._handleConfirm.bind(this);
      this._handleCancel = this._handleCancel.bind(this);
      this.doValidate = this.doValidate.bind(this);
      ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
    }

    _handleSelectContentType(){
      const {selected} = this.props;
      if (!selected){
        this._handleToggleDetailVisibility(true);
      } else {
        this.deselectContentType();
      }
    }

    deselectContentType(){
      const {contentType} = this.props;
      let {selectedContentTypes = {}} = this.props;
      const {context} = this.props;
      const content = selectedContentTypes[contentType.contentTypeId];
      if (content){
        selectedContentTypes = {...selectedContentTypes};
        selectedContentTypes[contentType.contentTypeId] = undefined;
        context.setState({selectedContentTypes});
      }
    }

    componentWillReceiveProps(nextProps) {
      ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
    }

    _handleToggleDetailVisibility(detailVisible){
      super.setState({detailVisible});
    }

    _handleOnModalHide(){
        
    }

    _handleCancel(){
      this._handleToggleDetailVisibility(false);
    }

    async _handleConfirm(){
      const {state} = this;
      const {context} = this.props;
      const result = await this.getValidationResult();
      if (!result || result.error == ''){
        if (context){
          let {selectedContentTypes = {}} = this.props;
          const {contentType, context} = this.props;
          selectedContentTypes = {...selectedContentTypes};
          let selectedProductCategories = state.content.selectedProductCategories || [];
          if (!selectedProductCategories.find(c=> c.categoryId === contentType.productCategory.categoryId)){
            selectedProductCategories = [...selectedProductCategories, contentType.productCategory];
          }
          selectedContentTypes[contentType.contentTypeId] = {...state.content, selectedProductCategories};
          context.setState({selectedContentTypes});
          this._handleToggleDetailVisibility(false);
        }
      } else {
        context.setState({showErrors: true});
      }
    }

    async getValidationResult(){
      const {contentType = {}} = this.props;
      const ContentTypeDetailControl = resolveDetailsControl(contentType);
      if (ContentTypeDetailControl.canSubmit ){
        return await ContentTypeDetailControl.canSubmit(this.state.content);
      }
      return undefined;
    }

    async doValidate(){
      const result = await this.getValidationResult();
      if (result){
        super.setState({canSubmit: !result || result.error == ''});
      }
    }

    setState(newState){
      const existingContent = this.state.content;
      const newContent = {...existingContent, ...newState};
      super.setState({content: newContent}, this.doValidate);
    }

    render(){
      const {resources} = this;
      const {selected, contentType = {}} = this.props;
      const {detailVisible, canSubmit, selectedContentTypes = {}} = this.state;
      const ContentTypeDetailControl = resolveDetailsControl(contentType);
      const stateForContentType = selectedContentTypes[contentType.contentTypeId] || {};
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
            {resources ? <TitleContainer title={resources.PageTitle(this.props)}/> : null }
            <ContentTypeDetailControl {...{contentType, ...this.props, ...stateForContentType, ...this.state.content, context: this, canSubmit}}/>
            <TouchableHighlight
              style={styles.confirmButton}
              underlayColor="#ebebeb"
              onPress={this._handleConfirm}>
              {confirmButton(!canSubmit)}
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


