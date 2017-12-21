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

    console.log(showDirections);
    if (showDirections){
      const route = await fetchRoute(client, {latitude: origin.latitude, longitude: origin.longitude}, {latitude: destination.latitude, longitude: destination.longitude});
      this.setState({route});
    }
  }

  render(){
    const rootUrl = 'https://maps.googleapis.com/maps/api/staticmap';
    const scale = 2;
    const { width, height, origin, destination} = this.props;

    const getMarkerParams = (markers) => {
      if (markers == undefined){
        return '';
      }

      const markerParams = markers.reduce((prev, curr, i) => {
        const location = curr.line1 !== undefined ? `${curr.latitude},${curr.longitude}` : '';
        return (i !== 0 ?  prev + '|' : '') + location;
      }, '');
      return `size:tiny|${markerParams}`;
    };

    const getPath = () => {
      const pathParams = this.state.route.coordinates.reduce((prev, curr, i) => {
        return (i !== 0 ?  prev + '|' : '') + `${curr.latitude},${curr.longitude}`;
      }, '');

      return `color:0x000000ff|weight:3|${pathParams}`;
    };
    const staticMapsUrl = `${rootUrl}?scale=${scale}&size=${width}x${height}&markers=${getMarkerParams([origin, destination])}&path=${getPath()}`;

    return <Image style={[...styles, this.props.style, {width, height}]} source={{uri: staticMapsUrl}} />;
  }
}

const styles = {};
