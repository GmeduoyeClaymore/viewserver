import deliveryIcon from './delivery-icon.png';
import toolIcon from './tool-icon.png';
import rubbishIcon from './rubbish-icon.png';
import skipIcon from './skip-icon.png';
import personellIcon from './personell-icon.png';
/*import tilerIcon from './tiler-icon.png';
import painterIcon from './painter-icon.png';
import brickLayerIcon from './brickLayer-icon.png';
import plumblerIcon from './plumber-icon.png';
import plastererIcon from './plasterer-icon.png';
import carpenterIcon from './carpenter-icon.png';
import groundWorkerIcon from './groundWorker-icon.png';*/

import deliveryIconSml from './delivery-icon-sml.png';
import toolIconSml from './tool-icon-sml.png';
import rubbishIconSml from './rubbish-icon-sml.png';
import skipIconSml from './skip-icon-sml.png';
import personellIconSml from './personell-icon-sml.png';

import unknownCategory from './unknownCategory-icon.png';
export {deliveryIcon};
export {toolIcon};
export const resolveContentTypeIcon = (contentType) => {
  switch (contentType.name.toLowerCase().replace(' ', '-')){
  case 'delivery':
    return deliveryIcon;
  case 'tool-hire':
    return toolIcon;
  case 'rubbish-collection':
    return rubbishIcon;
  case 'skip-hire':
    return skipIcon;
  case 'personell':
    return personellIcon;
  default:
    return deliveryIcon;
  }
};


export const resolveContentTypeIconSml = (contentType) => {
  switch (contentType.name.toLowerCase().replace(' ', '-')){
  case 'delivery':
    return deliveryIconSml;
  case 'tool-hire':
    return toolIconSml;
  case 'rubbish-collection':
    return rubbishIconSml;
  case 'skip-hire':
    return skipIconSml;
  case 'personell':
    return personellIconSml;
  default:
    return deliveryIconSml;
  }
};

export const resolveProductCategoryIcon = (categoryId) => {
  switch (categoryId.toLowerCase()){
  /*case '2tiler':
    return tilerIcon;
  case '2painter':
    return painterIcon;
  case '2bricklayer':
    return brickLayerIcon;
  case '2plumber':
    return plumblerIcon;
  case '2plasterer':
    return plastererIcon;
  case '2carpenters':
    return carpenterIcon;
  case '2groundWorkers':
    return groundWorkerIcon;*/
  default:
    return unknownCategory;
  }
};

export const resolveProductIcon = (product) => {
  switch (product.name.toLowerCase()){
  /*case '2tiler':
      return tilerIcon;
    case '2painter':
      return painterIcon;
    case '2bricklayer':
      return brickLayerIcon;
    case '2plumber':
      return plumblerIcon;
    case '2plasterer':
      return plastererIcon;
    case '2carpenters':
      return carpenterIcon;
    case '2groundWorkers':
      return groundWorkerIcon;*/
  default:
    return unknownCategory;
  }
};
