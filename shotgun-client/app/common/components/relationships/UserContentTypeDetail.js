import React, {Component} from 'react';
import {Icon} from 'common/components';
import {Text, ListItem} from 'native-base';
import {getFriendlyContentTypeName} from 'common/constants/ContentTypes';

export default class UserContentTypeDetail extends Component{
  render(){
    const {selectedContentTypes} = this.props;

    return Object.entries(selectedContentTypes).map(([contentTypeId, contentType]) => {
      const {selectedProductIds, selectedProductCategories} = contentType;

      return <ListItem padded key={contentTypeId} style={{flexWrap: 'wrap'}}>
        <Icon key='icon' style={styles.contentTypeIcon} name={`content-type-${contentTypeId}`}/>
        <Text key='text' >{getFriendlyContentTypeName(parseInt(contentTypeId, 10))}</Text>

        {selectedProductIds && selectedProductIds.map((p, i) =>{
          return  <Icon style={styles.productIcon} key={i} name={p}/>;
        })}

        {selectedProductCategories && selectedProductCategories.map((c, i) =>{
          return  <Icon key={i} style={styles.productIcon} name={c.categoryId}/>;
        })}
      </ListItem>;
    });
  }
}

const styles = {
  list: {
    flexWrap: 'wrap'
  },
  contentTypeIcon: {
    marginRight: 10,
    fontSize: 26
  },
  productIcon: {
    marginLeft: 10
  }
};
