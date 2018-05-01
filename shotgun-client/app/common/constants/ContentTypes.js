import Logger from 'common/Logger';

export const DELIVERY = 1;
export const HIRE = 3;
export const RUBBISH = 2;
export const SKIP = 4;
export const PERSONELL = 5;

export class ResourceDictionaryProperty{
  constructor(propertyName, resourceDictionary){
    this.propertyName = propertyName;
    this.resourceDictionary = resourceDictionary;
  }

  value(valuePair){
    if (Object.keys(valuePair).length != 1){
      throw new Error('Value pair must have two entries key and value');
    }
    const contentTypeId =  Object.keys(valuePair)[0];
    const dictionary = this.resourceDictionary.resolveInternal(contentTypeId);
    this.resourceDictionary.dictionaries[contentTypeId] = {...dictionary, ...{[this.propertyName]: valuePair[contentTypeId]}};
    return this;
  }

  delivery(value){
    return this.value({[DELIVERY]: value});
  }

  hire(value){
    return this.value({[HIRE]: value});
  }

  skip(value){
    return this.value({[SKIP]: value});
  }

  personell(value){
    return this.value({[PERSONELL]: value});
  }

  rubbish(value){
    return this.value({[RUBBISH]: value});
  }

  default(value){
    return this.value({DEFAULT: value});
  }

  property(propertyName, defaultValue){
    return this.resourceDictionary.property(propertyName, defaultValue);
  }

  resolve(contentTypeId){
    return this.resourceDictionary.resolve(contentTypeId);
  }
}

export const resolveResourceFromProps = (props, resourceDictionary, caller) => {
  const {selectedContentType, contentType, orderContentTypeId, order = {}} = props;
  if (order.orderContentTypeId){
    caller.resources = resourceDictionary.resolve(order.orderContentTypeId);
  } else if (orderContentTypeId){
    caller.resources = resourceDictionary.resolve(orderContentTypeId);
  } else if (selectedContentType){
    caller.resources = resourceDictionary.resolve(selectedContentType.contentTypeId);
  } else if (contentType){
    caller.resources = resourceDictionary.resolve(contentType.contentTypeId);
  } else {
    Logger.warning('Could not resolve resource dictionary as no content type was found in props');
    caller.resources = {};
  }
};

export class ResourceDictionary{
  constructor(){
    this.dictionaries = {};
    this.dictionaries[DELIVERY] = {};
    this.dictionaries[HIRE] = {};
    this.dictionaries[SKIP] = {};
    this.dictionaries[PERSONELL] = {};
    this.dictionaries[RUBBISH] = {};
    this.dictionaries.DEFAULT = {};
  }

  property(propertyName, defaultValue){
    if (!propertyName){
      throw new Error('Property name is a required value');
    }
    const result = new ResourceDictionaryProperty(propertyName, this);
    if (defaultValue){
      return result.default(defaultValue);
    }
    return result;
  }

  resolveInternal(contentTypeId){
    const dictionary = this.dictionaries[contentTypeId];
    if (!dictionary){
      throw new Error(`${contentTypeId} is not a valid content type value`);
    }
    if (!Object.keys(dictionary).length){
      return this.dictionaries.DEFAULT;
    }
    return dictionary;
  }

  resolve(contentTypeId){
    const dictionary = this.dictionaries[contentTypeId];
    if (!dictionary || !Object.keys(dictionary).length){
      return {...this.dictionaries.DEFAULT, contentTypeId};
    }
    return {...this.dictionaries.DEFAULT, ...dictionary, contentTypeId};
  }
}

export const bindToContentTypeResourceDictionary = (component, resourceDictionary) => {
  //if we're debugging then use the redux devtools extension
  let {componentWillReceiveProps, componentWillMount} = component;
  if (componentWillReceiveProps){
    componentWillReceiveProps = componentWillReceiveProps.bind(component);
  }
  if (componentWillMount){
    componentWillMount = componentWillMount.bind(component);
  }

  component.componentWillReceiveProps = (props) => {
    resolveResourceFromProps(props, resourceDictionary, component);
    if (componentWillReceiveProps){
      componentWillReceiveProps(props);
    }
  };

  component.componentWillMount = () => {
    resolveResourceFromProps(component.props, resourceDictionary, component);
    if (componentWillMount){
      componentWillMount();
    }
  };
};
