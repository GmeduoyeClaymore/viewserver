import React, { Component } from 'react';
import PropTypes from 'prop-types';
import MapView from 'react-native-maps';
import {fetchRoute} from './MapUtils';

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

	fetchAndRenderRoute = () => {
	  const {
	    onReady,
	    onError,
	    client,
	    origin,
	    destination
	  } = this.props;


	  fetchRoute(client, origin, destination)
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
