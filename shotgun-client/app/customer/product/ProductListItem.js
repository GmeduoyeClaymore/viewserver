import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';
import { withRouter } from 'react-router';

const ProductListItem = ({history, product}) => {
  const styles = StyleSheet.create({
    container: {
      flex: 1,
      flexDirection: 'row',
      padding: 5
    },
    picture: {
      width: 80,
      height: 80,
      borderRadius: 20,
      marginRight: 8
    },
    title: {
      fontWeight: 'bold',
      color: '#848484'
    }
  });

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80}} onPress={() => history.push('/CustomerLanding/ProductDetails', {product})} underlayColor={'#EEEEEE'}>
    <View style={styles.container}>
      <Image source={require('../assets/cement.jpg')} style={styles.picture} />
      <View style={{flex: 1}}>
        <Text style={styles.title}>{product.name}</Text>
        <Text>{product.description}</Text>
      </View>
    </View>
  </TouchableHighlight>;
};

ProductListItem.propTypes = {
  product: PropTypes.object,
  history: PropTypes.object
};

export default withRouter(ProductListItem);


