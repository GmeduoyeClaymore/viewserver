import React, {Component,PropTypes} from 'react';
import { View, ListView, Text, Image, StyleSheet, TouchableOpacity } from 'react-native';
import ProductActionBar from './ProductActionBar';

export default class ProductDetails extends Component {

    static PropTypes = {
        customerService : PropTypes.object,
        product : PropTypes.object
    }

    constructor(props) {
        super(props);
    }

    render() {
        const { product,customerService } = this.props;
        const { shoppingCartDao } = customerService;
        if (product) {
            return (
                <View style={styles.container}>
                    <View style={styles.header}>
                        <Image source={require('./assets/cement.jpg')} style={styles.picture} />
                        <Text style={styles.bigText}>{product['P_name']}</Text>
                        <Text style={[styles.mediumText, styles.lightText]}>{product['P_description']}</Text>
                        <ProductActionBar product={product} shoppingCartDao={shoppingCartDao}/>
                    </View>
                </View>
            );
        } else {
            return null;
        }
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