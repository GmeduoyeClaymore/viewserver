import React, { Component } from 'react';
import {Image} from 'react-native';
import {fetchRoute} from './MapUtils';

export default class MapViewStatic extends Component {
  constructor (props) {
    super(props);
    this.state = {
      route: {
        coordinates: []
      }
    };
  }

  async componentDidMount() {
    const {client, origin, destination} = this.props;
    const showDirections = origin.line1 !== undefined && destination.line1 !== undefined;

    if (showDirections){
      const route = await fetchRoute(client, [origin, destination]);
      this.setState({route});
    }
  }

  render(){
    const rootUrl = 'https://maps.googleapis.com/maps/api/staticmap';
    const { origin, destination} = this.props;
    let { width, height} = this.props;
    const scale = 2;
    const zoom = destination.line1 == undefined ? '&zoom=14' : '';
    width = Math.round(width);
    height = Math.round(height);


    const getOriginMarker = (origin) => {
      return `&markers=icon:${encodeURI('https://s3.eu-west-2.amazonaws.com/shotgunassets/pin-origin-small.png')}|${origin.latitude},${origin.longitude}`;
    };

    const getDestinationMarker = (destination) => {
      return destination.line1 !== undefined ? `&markers=icon:${encodeURI('https://s3.eu-west-2.amazonaws.com/shotgunassets/pin-destination-small.png')}|${destination.latitude},${destination.longitude}` : '';
    };

    const getPath = () => {
      const pathParams = this.state.route.coordinates.reduce((prev, curr, i) => {
        return (i !== 0 ?  prev + '|' : '') + `${curr.latitude},${curr.longitude}`;
      }, '');

      return `color:0x000000ff|weight:3|${pathParams}`;
    };
    const staticMapsUrl = `${rootUrl}?style=feature:poi|visibility:off${zoom}&scale=${scale}&size=${width}x${height}${getOriginMarker(origin)}${getDestinationMarker(destination)}&path=${getPath()}`;

    return <Image style={[styles, this.props.style, {width, height}]} source={{uri: staticMapsUrl}} />;
  }
}

const styles = {
  borderRadius: 4
};
