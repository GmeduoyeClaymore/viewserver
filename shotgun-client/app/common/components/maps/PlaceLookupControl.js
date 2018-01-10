import React, { Component } from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import {Text, Button, Content, Header, Left, Body, Container, Icon, Title} from 'native-base';
const MAX_RECENT_ADDRESSES = 10;

class PlacesLookupControl extends Component{
    static propTypes = {
        client: PropTypes.object,
        addressLabel: PropTypes.string.isRequired,
        OnAddressSeleted: PropTypes.func.isRequired
    }

    constructor (props) {
        super(props);
    }

    getOrderedAddresses(addreses){
        if (!addreses){
            return addreses;
        }
        let result = [...addreses];
        result.sort((e1, e2) => moment(e1.created).compareTo(moment(e2.created)));
        result = result.slice(0, MAX_RECENT_ADDRESSES);
        return result.map(address => {
            return {
                text: this.getAddressText(address),
                city: this.getCity(address),
                address
            };
        });
    }

    getAddressText(address){
        const result = [];
        if (address.flatNumber){
            result.push('Fl' + address.flatNumber);
        }
        if (address.line1){
            result.push(address.line1);
        }
        if (address.line2){
            result.push(address.line2);
        }
        if (address.postCode){
            result.push(address.postCode);
        }
        return  result.join(',');
    }

    getCity(address){
        return  address.city;
    }

    renderAddress(addressSummary){
        const {OnAddressSeleted} = this.props;
        const {text, city, address} = addressSummary;
        return <Content onClick={() => OnAddressSeleted(address)}>
            <Row style={{fontWeight: 'bold'}}>
                <Text>{text}</Text>;
            </Row>
            <Row style={{fontWeight: 'normal'}}>
                <Text>{city}</Text>;
            </Row>
        </Content>;
    }

    onAddressChanged(value){
        this.setState({addressSeachText: value}, () => searchAutoCompleteSuggestions(value));
    }

    buildRowsFromResults(results){
        return results;
    }

    async searchAutoCompleteSuggestions(value){
        const { client } = this.props;
        const responseJSON = await client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {input: value, language: 'en'});
        this.setState({
            predictedResults: this.buildRowsFromResults(responseJSON.predictions),
        });
    }

    render(){
        const {errors, addressSeachText, predictedResults} = this.state;
        const {deliveryAddresses, addressLabel} = this.props;
        return (
            <Container>
                <Header>
                    <Left>
                    <Button transparent disabled={busy}>
                        <Icon name='close-circle' onPress={() => history.goBack()} />
                    </Button>
                    </Left>
                    <Body><Title>{addressLabel}</Title></Body>
                </Header>
                <ErrorRegion errors={errors}>
                    <ValidatingInput placeholder={'Search ' + addressLabel} value={addressSeachText} onChangeText={this.onAddressChanged} validationSchema={VehicleDetails.validationSchema.registrationNumber}/>
                </ErrorRegion>
                {predictedResults && predictedResults.length ? <Container>
                    <Text>Results</Text>
                    {predictedResults.map(c=> this.renderAddress(c))}
                </Container> : null}
                {deliveryAddresses && deliveryAddresses.length ? <Container>
                    <Text>Recent Addresses</Text>
                    {deliveryAddresses.map(c=> this.renderAddress(c))}
                </Container> : null}
                
            </Container>
        );
    }
}

const mapStateToProps = (state, initialProps) => ({
    ...getDaoState(state, ['customer'], 'deliveryAddressDao'),
    ...initialProps
});
  
export default connect(mapStateToProps)(PlacesLookupControl);
