import React from 'react';
import ImagePicker from 'react-native-image-crop-picker';
import {ActionSheet} from 'native-base';
import Logger from 'common/Logger';
import PermissionsService from 'common/services/PermissionsService';

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
      await PermissionsService.requestCameraPermission();
      Logger.info('Launching camera');
      const response = await ImagePicker.openCamera({...ImageSelector.options, ...options});
      onSelect(response);
      Logger.info('Finished Launching camera');
    } catch (ex){
      Logger.warning(ex);
    }
  }

  static async launchPicker(onSelect, options){
    try {
      await PermissionsService.requestPickerPermission();
      Logger.info('Launching picker');
      const response = await ImagePicker.openPicker({...ImageSelector.options, ...options});
      onSelect(response);
      Logger.info('Finished Launching picker');
    } catch (ex){
      Logger.warning(ex);
    }
  }

  static show({title, onSelect, options}){
    if (!ActionSheet){
      throw new Error('Action sheet not set ?????');
    }
    try {
      ActionSheet.show(
        {
          options: ['Take photo', 'Choose from library', 'Cancel'],
          cancelButtonIndex: 2,
          title
        },
        buttonIndex => {
          buttonIndex = typeof buttonIndex == 'number' ? buttonIndex : parseInt(buttonIndex, 10);

          switch (buttonIndex) {
          case 0:
            ImageSelector.launchCamera(onSelect, options);
            break;
          case 1:
            ImageSelector.launchPicker(onSelect, options);
            break;
          default:
            Logger.warning('No selection detected');
            break;
          }
        }
      );
    } catch (error){
      Logger.warning(error);
    }
  }
}
