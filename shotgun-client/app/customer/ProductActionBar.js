import React, {Component,PropTypes} from 'react';
import {View, Text, Image, StyleSheet, TouchableOpacity, TextInput} from 'react-native';
import ActionButton from '../common/ActionButton.js'
import icon from  "../common/assets/basket-fill.png"
export default class ProductActionBar extends Component {

    state = {
        itemCount : 1
    }

    static PropTypes = {
        product : PropTypes.object,
        shoppingCartDao : PropTypes.object
    }

    constructor(props){
        super(props)
        this.addToShopping = this.addToShopping.bind(this);
    }

    async addToShopping() {
        try
        {
            this.setState({busy : true});
            const {itemCount} = this.state;
            const {product,shoppingCartDao} = this.props;
            const {P_ID} = product;
            let items = await shoppingCartDao.addItemtoCart(P_ID,itemCount);
        }
        finally{
            this.setState({busy : false});
        }
    }

    onItemCountChange(itemCount) {
        this.setState({itemCount : itemCount});
    }

    render() {
        const {busy} = this.state;
        return (
            <View style={styles.container}>
                <TextInput 
                    keyboardType = 'numeric'
                    onChangeText = {(text)=> this.onItemCountChange(text)}
                    value = {this.state.itemCount + ""}
                    maxLength = {10}  //setting limit of input
                />
                {!busy? <ActionButton buttonText="Add To Basket" icon={icon} action={this.addToShopping}/> : null}
            </View>
        );
    }
}


const styles = StyleSheet.create({
    container: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        backgroundColor: '#FAFAFF',
        paddingVertical: 8
    },
    action: {
        flex: 1,
        alignItems: 'center'
    },
    actionText: {
        color: '#007AFF'
    },
    icon: {
        height: 20,
        width: 20
    }
});
