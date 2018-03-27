import React, { Component } from 'react';
import PropTypes from 'prop-types';
import MapView from 'react-native-maps';
import {fetchRoute} from './MapUtils';
import {setStateIfIsMounted} from 'custom-redux';
import Logger from 'common/Logger';

class MapViewDirections extends Component {
  constructor(props) {
    super(props);

    this.state = {
      coordinates: null,
      distance: null,
      duration: null,
    };
    setStateIfIsMounted(this);
    this.fetchAndRenderRoute = this.fetchAndRenderRoute.bind(this);
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
	};

	fetchAndRenderRoute(){
	  const {
	    onReady,
	    onError,
	    client,
	    locations
	  } = this.props;
    
	  if (!client){
	    throw new Error('Client must be defined');
	  }

	  fetchRoute(client, locations)
	    .then(result => {
	      this.setState(result);
	      onReady && onReady(result);
	    })
	    .catch(errorMessage => {
	      this.resetState();
	      Logger.warning('MapViewDirections Error', errorMessage);
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
