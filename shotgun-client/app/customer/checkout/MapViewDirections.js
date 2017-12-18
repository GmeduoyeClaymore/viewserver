import React, { Component } from 'react';
import PropTypes from 'prop-types';
import MapView from 'react-native-maps';

class MapViewDirections extends Component {
  constructor(props) {
    super(props);

    this.state = {
      coordinates: null,
      distance: null,
      duration: null,
    };
  }

  componentDidMount() {
    this.fetchAndRenderRoute();
  }

  componentWillReceiveProps(nextProps) {
    if ((nextProps.origin != this.props.origin) || (nextProps.destination != this.props.destination)) {
      this.resetState(this.fetchAndRenderRoute);
    }
  }

	resetState = (cb = null) => {
	  this.setState({
	    coordinates: null,
	    distance: null,
	    duration: null,
	  }, cb);
	}

	decode(t, e) {
  	const d = [];

	  for (let n, o, u = 0, l = 0, r = 0, h = 0, i = 0, a = null, c = Math.pow(10, e || 5); u < t.length;) {
	    a = null, h = 0, i = 0;
	    do a = t.charCodeAt(u++) - 63, i |= (31 & a) << h, h += 5; while (a >= 32);
	    n = 1 & i ? ~(i >> 1) : i >> 1, h = i = 0;
	    do a = t.charCodeAt(u++) - 63, i |= (31 & a) << h, h += 5; while (a >= 32);
	    o = 1 & i ? ~(i >> 1) : i >> 1, l += n, r += o, d.push([l / c, r / c]);
	  }

	  return d.map((t) => {
	    return {
	      latitude: t[0],
	      longitude: t[1]
	    };
	  });
	}

	fetchAndRenderRoute = () => {
	  const {
	    onReady,
	    onError,
	  } = this.props;

	  let {
	    origin,
	    destination,
	  } = this.props;

	  if (origin.latitude && origin.longitude) {
	    origin = `${origin.latitude},${origin.longitude}`;
	  }

	  if (destination.latitude && destination.longitude) {
	    destination = `${destination.latitude},${destination.longitude}`;
	  }

	  this.fetchRoute(origin, destination)
	    .then(result => {
	      this.setState(result);
	      onReady && onReady(result);
	    })
	    .catch(errorMessage => {
	      this.resetState();
	      console.warn(`MapViewDirections Error: ${errorMessage}`);
	      onError && onError(errorMessage);
	    });
	}

	fetchRoute = async  (origin, destination) => {
	  const mode = 'driving';
	  const json = await this.props.client.invokeJSONCommand('mapsController', 'mapDirectionRequest', {origin, destination, mode});
	  if (json.routes.length){
	    const route = json.routes[0];
	    return {
	      distance: route.legs.reduce((carry, curr) => {
	        return carry + curr.distance.value;
	      }, 0) / 1000,
	      duration: route.legs.reduce((carry, curr) => {
	        return carry + curr.duration.value;
	      }, 0) / 60,
	      coordinates: this.decode(route.overview_polyline.points)
	    };
	  }
	  throw new Error('No routes returned');
	}

	render() {
	  if (!this.state.coordinates) {
	    return null;
	  }
	  return <MapView.Polyline coordinates={this.state.coordinates} {...this.props} />;
	}
}

MapViewDirections.propTypes = {
  origin: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.shape({
      latitude: PropTypes.number.isRequired,
      longitude: PropTypes.number.isRequired,
    }),
  ]),
  destination: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.shape({
      latitude: PropTypes.number.isRequired,
      longitude: PropTypes.number.isRequired,
    }),
  ]),
  onReady: PropTypes.func,
  onError: PropTypes.func,
};

export default MapViewDirections;
