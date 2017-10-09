import React, {Component} from 'react';
import {Text, TouchableOpacity, Image, StyleSheet,View} from 'react-native';
import {Navigator} from 'react-native-deprecated-custom-components';
import ProductList from './customer/ProductList';
import ProductDetails from './customer/ProductDetails';

export default class Landing extends Component {

    constructor(props){
      super(props);
      this.renderScene = this.renderScene.bind(this);
    }

    renderScene(route, navigator) {
        switch (route.name) {
            case 'product-list':
                return <ProductList navigator={navigator} client={this.props.client}/>
            case 'product-details':
                return <ProductDetails navigator={navigator} product={route.data} />
        }
    }

    render() {
        return (
            <Navigator
                initialRoute={{name: 'product-list', title: 'Product List'}}
                renderScene={(rt,nav) => <View style={{marginTop : 60,flex : 1}}>{this.renderScene(rt,nav)}</View>}
                navigationBar={
                    <Navigator.NavigationBar
                        routeMapper={{
                            LeftButton: (route, navigator, index, navState) => {
                                if (route.name === 'product-list') {
                                    return null;
                                } else {
                                    return (
                                        <TouchableOpacity onPress={() => navigator.pop()}>
                                            <Image source={require('./assets/back.png')} style={styles.backButton} />
                                        </TouchableOpacity>
                                    );
                                }
                            },
                            RightButton: (route, navigator, index, navState) => {
                                return null;
                            },
                            Title: (route, navigator, index, navState) => {
                                return (<Text style={styles.title}>{route.title}</Text>);
                            },
                        }}
                        style={styles.navBar}
                    />
                }
            />
        )
    }
}

const styles = StyleSheet.create({
    navBar: {
        backgroundColor: '#FAFAFF',
        height: 60,
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
