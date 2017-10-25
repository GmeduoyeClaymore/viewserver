import React, {Component} from 'react';
import {Text, TouchableOpacity, Image, StyleSheet, View} from 'react-native';
import {Navigator} from 'react-native-deprecated-custom-components';
import ProductList from '../customer/ProductList';
import ProductDetails from '../customer/ProductDetails';
import CustomerMenuBar from '../customer/CustomerMenuBar';
import ViewShoppingCart from '../customer/ViewShoppingCart';
import CustomerServiceFactory from '../customer/data/CustomerServiceFactory';
import Logger from '../viewserver-client/Logger';

export default class CustomerLanding extends Component {
  constructor(props){
    super(props);
    this.state = {
      isReady: false
    };
    this.renderScene = this.renderScene.bind(this);
    this.customerServiceFactory = new CustomerServiceFactory(this.props.client);
  }


  async componentWillMount() {
    try {
      this.customerService = await this.customerServiceFactory.create(this.props.principal.customerId);
    } catch (error){
      Logger.error(error);
    }
    Logger.debug('Network connected !!');
    this.setState({ isReady: true });
  }
    

  renderScene(route, navigator) {
    return <View style={{flexDirection: 'column', flex: 1}}>
      <CustomerMenuBar style={{minHeight: 45}} navigator={navigator} shoppingCartDao={this.customerService.shoppingCartDao}/>
      {this.renderSceneContent(route, navigator)}
    </View>;
  }
    
  renderSceneContent(route, navigator) {
    switch (route.name) {
      case 'product-list':
        return <ProductList style={styles.contentStyle} navigator={navigator} client={this.props.client}/>;
      case 'product-details':
        return <ProductDetails style={styles.contentStyle} navigator={navigator} product={route.data} customerService={this.customerService}/>;
      case 'view-shopping-cart':
        return <ViewShoppingCart style={styles.contentStyle} shoppingCartItems={route.data} customerService={this.customerService}/>;
      default:
        return undefined;
    }
  }

  render() {
    if (!this.state.isReady) {
      return null;
    }
      
    return (
      <Navigator
        initialRoute={{name: 'product-list', title: 'Product List'}}
        renderScene={(rt, nav) => <View style={{marginTop: 60, flex: 1}}>{this.renderScene(rt, nav)}</View>}
        navigationBar={
          <Navigator.NavigationBar
            routeMapper={{
              LeftButton: (route, navigator) => {
                if (route.name === 'product-list') {
                  return null;
                }
                return (
                  <TouchableOpacity onPress={() => navigator.pop()}>
                    <Image source={require('../common/assets/back.png')} style={styles.backButton} />
                  </TouchableOpacity>
                );
              },
              RightButton: () => {
                return null;
              },
              Title: (route) => {
                return (<Text style={styles.title}>{route.title}</Text>);
              },
            }}
            style={styles.navBar}
          />
        }
      />
    );
  }
}

const styles = StyleSheet.create({
  navBar: {
    backgroundColor: '#FAFAFF',
    height: 60,
  },
  contentStyle: {
    flex: 1
  },
  backButton: {
    marginTop: 8,
    marginLeft: 12,
    height: 24,
    width: 24
  },
  title: {
    padding: 8,
    fontSize: 16,
    fontWeight: 'bold'
  }
});
