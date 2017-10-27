import React, {Component, PropTypes} from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import ProductActionBar from './ProductActionBar';

export default class ProductDetails extends Component {
    static PropTypes = {
      customerService: PropTypes.object,
      product: PropTypes.object
    };

    static navigationOptions = ({ navigation }) => ({
      title: `${navigation.state.params.product.name}`,
    });

    constructor(props) {
      super(props);
    }

    render() {
      const {customerService} = this.props.screenProps;
      const { product } = this.props.navigation.state.params;
      if (product) {
        return (
          <View style={styles.container}>
            <View style={styles.header}>
              <Image source={require('./assets/cement.jpg')} style={styles.picture} />
              <Text style={styles.bigText}>{product.name}</Text>
              <Text style={[styles.mediumText, styles.lightText]}>{product.description}</Text>
              <ProductActionBar product={product} orderItemsDao={customerService.orderItemsDao}/>
            </View>
          </View>
        );
      }
      return null;
    }
}

const styles = StyleSheet.create({
  container: {
    marginTop: 60,
    backgroundColor: '#FFFFFF',
    flex: 1
  },
  header: {
    alignItems: 'center',
    backgroundColor: '#FAFAFF',
    paddingBottom: 4,
    borderBottomColor: '#F2F2F7',
    borderBottomWidth: StyleSheet.hairlineWidth
  },
  manager: {
    paddingBottom: 10,
    alignItems: 'center'
  },
  picture: {
    width: 80,
    height: 80,
    borderRadius: 40
  },
  smallPicture: {
    width: 40,
    height: 40,
    borderRadius: 20
  },
  mediumText: {
    fontSize: 16,
  },
  bigText: {
    fontSize: 20
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  list: {
    flex: 1,
  },
  emptyList: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  lightText: {
    color: '#C7C7CC'
  }
});
