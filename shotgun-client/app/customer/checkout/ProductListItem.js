import React from 'react';
import PropTypes from 'prop-types';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';


const selectProduct = ({history, context, product}) => {
  context.setState({selectedProduct: product});
  history.push('/Customer/Checkout/ProductCategoryList');
};

const ProductListItem = ({history, context, product}) => {
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

  return <TouchableHighlight style={{flex: 1, flexDirection: 'row', minHeight: 80}} onPress={() => selectProduct({history, context, product}) } underlayColor={'#EEEEEE'}>
    <View style={styles.container}>
      <Image source={{uri: 'https://media.istockphoto.com/vectors/minimalistic-solid-line-colored-builder-icon-vector-id495391344?k=6&m=495391344&s=612x612&w=0&h=SFsgxOa-pdm9NTbc3NVj-foksXnqyPW3LhNjJtQLras='}} style={styles.picture} />
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

export default ProductListItem;


