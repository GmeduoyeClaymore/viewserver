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
    if (!Object.keys(valuePair).length == 2){
      throw new Error('Value pair must have two entries key and value');
    }
    const contentTypeId =  Object.keys(valuePair)[0];
    const dictionary = this.resourceDictionary.resolve(contentTypeId, true);
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

  property(propertyName){
    return this.resourceDictionary.property(propertyName);
  }
}

export const resolveResourceFromProps = (props, resourceDictionary, caller) => {
  const {selectedContentType} = props;
  if (selectedContentType){
    caller.resources = resourceDictionary.resolve(selectedContentType.contentTypeId);
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

  resolve(contentTypeId, strict){
    const dictionary = this.dictionaries[contentTypeId];
    if (!dictionary){
      if (strict){
        throw new Error(`${contentTypeId} is not a valid content type value`);
      }
      return dictionary.DEFAULT;
    }
    return dictionary;
  }
}
