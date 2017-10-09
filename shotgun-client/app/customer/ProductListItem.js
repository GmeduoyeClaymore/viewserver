import React, { Component } from 'react';
import { View, Text, Image, TouchableHighlight, StyleSheet } from 'react-native';

export default class EmployeeListItem extends Component {

    showDetails() {
        this.props.navigator.push({name: 'product-details', data: this.props.data});
    }

    render() {
        return (
            <TouchableHighlight style={{flex : 1,flexDirection: 'row',minHeight : 80}} onPress={this.showDetails.bind(this)} underlayColor={'#EEEEEE'}>
                <View style={styles.container}>
                    <Image source={require('./assets/cement.jpg')} style={styles.picture} />
                    <View style={{flex : 1}}>
                        <Text style={styles.title}>{this.props.item['P_name']}</Text>
                        <Text>{this.props.item['P_description']}</Text>
                    </View>
                </View>
            </TouchableHighlight>
        )
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'row',
        padding : 5
    },
    picture: {
        width: 80,
        height: 80,
        borderRadius: 20,
        marginRight: 8
    },
    title: {
        fontWeight : 'bold',
        color: '#848484'
    }
});
