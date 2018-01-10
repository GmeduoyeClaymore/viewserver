import React, { Component } from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import {Text, Button, Content, Header, Left, Body, Container, Icon, Title, Input, Row} from 'native-base';
import {connect} from 'react-redux';
import {getDaoState} from 'common/dao';
import MapUtils from 'common/services/MapUtils';
import ErrorRegion from 'common/components/ErrorRegion';
import {debounce} from 'lodash';
const MAX_RECENT_ADDRESSES = 10;

class AddressLookup extends Component{
    static propTypes = {
        client: PropTypes.object,
        addressLabel: PropTypes.string.isRequired
    }


    state = {
        busy: false
    };

    constructor (props) {
        super(props);
        this.onAddressChanged = this.onAddressChanged.bind(this);
        this.searchAutoCompleteSuggestions = debounce(this.searchAutoCompleteSuggestions, 500);
    }


    compare(m1, m2){
        if (m1.isAfter(m2)){
            return 1;
        }
        if (m2.isAfter(m1)){
            return -1;
        }
        return 0;
    }

    getOrderedAddresses(addreses){
        if (!addreses){
            return addreses;
        }
        let result = [...addreses];
        result.sort((e1, e2) => this.compare(moment(e1.created), moment(e2.created)));
        result = result.slice(0, MAX_RECENT_ADDRESSES);
        return result.map(address => {
            return this.toSummary(address);
        });
    }


    toSummary(address){
        return {
            text: MapUtils.getAddressText(address),
            city: this.getCity(address),
            address: {...address, googlePlaceId: address.googlePlacesId}
        };
    }


    getCity(address){
        return  address.city;
    }

    OnAddressSeleted(addreses){
        const {context, history, addressKey} = this.props;
        context.setState({[addressKey]: addreses}, () => history.push('/Customer/Checkout/DeliveryMap'));
    }

    renderAddress(addressSummary, idx){
        const {text, city, address} = addressSummary;
        return <Content key={idx} style={{minHeight: 20}} >
                <Text  onPress={() => this.OnAddressSeleted(address)}  style={{fontWeight: 'bold'}} >{text}</Text>
                <Text  onPress={() => this.OnAddressSeleted(address)}>{city}</Text>
            </Content>;
    }

    async OnAddressPredictionSeleted(rowData){
        const {client} = this.props;
        try {
            const res = await client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
                placeid: rowData.place_id,
                language: 'en'
              }).timeoutWithError(5000, 'Place request timed out');

            this.OnAddressSeleted(MapUtils.parseGooglePlacesData(res.result));
        } catch (error){
            this.setState({error});
        }
    }

    renderAddressPrediction(prediction, idx){
        const {description} = prediction;
        return <Content key={idx} style={{minHeight: 10}} >
                <Text  onPress={() => this.OnAddressPredictionSeleted(prediction)}  style={{fontWeight: 'bold'}} >{description}</Text>
            </Content>;
    }

    onAddressChanged(value){
        this.setState({addressSearchText: value}, () => this.searchAutoCompleteSuggestions(value));
    }

    buildRowsFromResults(results){
        return results.map(res => this.toSummary(MapUtils.parseGooglePlacesData(res)));
    }

    async searchAutoCompleteSuggestions(value){
        const { client } = this.props;
        try {
            this.setState({busy: true});
            const responseJSON = await client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {input: value, language: 'en'});
            this.setState({
                predictedResults: responseJSON.predictions,
                busy: false
            });
        } catch (error){
            this.setState({error});
        }
    }

    render(){
        const {errors, addressSearchText, predictedResults = [], busy} = this.state;
        const {deliveryAddresses = [], addressLabel} = this.props;
        const orderedAddresses = this.getOrderedAddresses(deliveryAddresses);
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
                    <Input placeholder={'Search ' + addressLabel} value={addressSearchText} onChangeText={this.onAddressChanged}/>
                </ErrorRegion>
            {predictedResults && predictedResults.length ? <Container>
                    <Text>Results</Text>
                    {predictedResults.map((c, idx) => this.renderAddressPrediction(c, idx))}
                </Container> : null}
                {deliveryAddresses && deliveryAddresses.length ? <Container>
                    <Text>Recent Addresses</Text>
                    {orderedAddresses.map((c, idx)=> this.renderAddress(c, idx))}
                </Container> : null}
                
            </Container>
        );
    }
}

const mapStateToProps = (state, initialProps) => ({
    ...(initialProps && initialProps.location && initialProps.location.state ? initialProps.location.state : {}),
    ...getDaoState(state, ['customer'], 'deliveryAddressDao'),
    ...initialProps
});
  
export default connect(mapStateToProps)(AddressLookup);
