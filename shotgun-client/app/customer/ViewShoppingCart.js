import React, {Component,PropTypes} from 'react';
import { View, ListView, Text, Image, StyleSheet, TouchableOpacity } from 'react-native';
import ActionButton from "../common/ActionButton"
import icon from  "../common/assets/truck-fast.png"


export default class ViewShoppingCart extends Component {
    static PropTypes = {
        shoppingCartItems : PropTypes.array
    }

    constructor(props){
        super(props)
    }

    renderItem(item){
        return <View key={item.key} style={{flexDirection : 'column', flex : 1}}>
            <Text>{"Product: " + item.ProductId}</Text>
            <Text>{"Quantity: " + item.ProductQuantity}</Text>
        </View>
    }

    purchaseItems(){
    }

    render(){
        const { shoppingCartItems } = this.props;

        return <View style={{flex : 1,flexDirection : 'column'}}>
            {shoppingCartItems.map( c => this.renderItem(c))}
            <ActionButton buttonText="Purchase" icon={icon} action={this.purchaseItems}/>
        </View>
    }
}