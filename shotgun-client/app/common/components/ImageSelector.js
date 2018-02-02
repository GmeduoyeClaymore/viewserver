import React from 'react';
import ImagePicker from 'react-native-image-crop-picker';
import {ActionSheet} from 'native-base';
import Logger from 'common/Logger';

export class ImageSelector{
  static options = {
    cropping: false,
    cropperCircleOverlay: false,
    compressImageQuality: 0.5,
    mediaType: 'photo',
    useFrontCamera: false,
    includeBase64: true};

  static async launchCamera(onSelect, options){
    try {
      const response = await ImagePicker.openCamera({...ImageSelector.options, ...options});
      onSelect(response);
    } catch (ex){
      Logger.warning(ex);
    }
  }

  static async launchPicker(onSelect, options){
    try {
      const response = await ImagePicker.openPicker({...ImageSelector.options, ...options});
      onSelect(response);
    } catch (ex){
      Logger.warning(ex);
    }
  }

  static show({title, onSelect, options}){
    ActionSheet.show(
      {
        options: ['Take photo', 'Choose from library', 'Cancel'],
        cancelButtonIndex: 2,
        title
      },
      buttonIndex => {
        switch (buttonIndex) {
        case '0':
          ImageSelector.launchCamera(onSelect, options);
          break;
        case '1':
          ImageSelector.launchPicker(onSelect, options);
          break;
        default:
          break;
        }
      }
    );
  }
}
