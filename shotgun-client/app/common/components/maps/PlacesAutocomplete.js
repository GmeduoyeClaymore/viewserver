import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Logger from 'common/Logger';
import {
  TextInput,
  View,
  FlatList,
  ScrollView,
  Text,
  StyleSheet,
  Dimensions,
  TouchableHighlight,
  Platform,
  ActivityIndicator,
  PixelRatio
} from 'react-native';
import {debounce} from 'lodash';

const WINDOW = Dimensions.get('window');

const defaultStyles = {
  container: {
  },
  textInputContainer: {
    backgroundColor: '#C9C9CE',
    height: 40,
    borderTopColor: '#7e7e7e',
    borderBottomColor: '#b5b5b5',
    borderTopWidth: 1 / PixelRatio.get(),
    borderBottomWidth: 1 / PixelRatio.get(),
    flexDirection: 'row',
  },
  textInput: {
    backgroundColor: '#FFFFFF',
    height: 40,
    paddingLeft: 0,
    paddingRight: 5,
    lineHeight: 24,
    fontSize: 18,
    flex: 1
  },
  poweredContainer: {
    justifyContent: 'flex-end',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  powered: {},
  listView: {
    // flex: 1,
  },
  row: {
    padding: 13,
    height: 44,
    flexDirection: 'row',
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#c8c7cc',
  },
  description: {},
  loader: {
    // flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-end',
    height: 20,
  },
  androidLoader: {
    marginRight: -15,
  },
};

export default class PlacesAutocomplete extends Component {
  constructor (props) {
    super(props);
    this.state = this.getInitialState.call(this);
  }

  getInitialState = () => ({
    text: this.props.getDefaultValue(),
    dataSource: this.buildRowsFromResults([]),
    listViewDisplayed: this.props.listViewDisplayed === 'auto' ? false : this.props.listViewDisplayed,
  })

  setAddressText = address => this.setState({ text: address })

  getAddressText = () => this.state.text

  buildRowsFromResults = (results) => {
    let res = [];

    if (results.length === 0 || this.props.predefinedPlacesAlwaysVisible === true) {
      res = [...this.props.predefinedPlaces];

      if (this.props.currentLocation === true) {
        res.unshift({
          description: this.props.currentLocationLabel,
          isCurrentLocation: true,
        });
      }
    }

    res = res.map(place => ({
      ...place,
      isPredefinedPlace: true
    }));

    return [...res, ...results];
  }

  componentWillMount() {
    this._request = this.props.debounce
      ? debounce(this._request, this.props.debounce)
      : this._request;
  }

  componentDidMount() {
    this._onChangeText(this.state.text);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.listViewDisplayed !== 'auto') {
      this.setState({
        listViewDisplayed: nextProps.listViewDisplayed,
      });
    }

    if (typeof(nextProps.text) !== 'undefined' && this.state.text !== nextProps.text) {
      this.setState({
        listViewDisplayed: true
      }, this._handleChangeText(nextProps.text));
    }
  }


  /**
   * This method is exposed to parent components to focus on textInput manually.
   * @public
   */
  triggerFocus = () => {
    if (this.refs.textInput) this.refs.textInput.focus();
  }

  /**
   * This method is exposed to parent components to blur textInput manually.
   * @public
   */
  triggerBlur = () => {
    if (this.refs.textInput) this.refs.textInput.blur();
  }

  getCurrentLocation = () => {
    let options = {
      enableHighAccuracy: false,
      timeout: 20000,
      maximumAge: 1000
    };

    if (this.props.enableHighAccuracyLocation && Platform.OS === 'android') {
      options = {
        enableHighAccuracy: true,
        timeout: 20000
      };
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        if (this.props.nearbyPlacesAPI === 'None') {
          const currentLocation = {
            description: this.props.currentLocationLabel,
            geometry: {
              location: {
                lat: position.coords.latitude,
                lng: position.coords.longitude
              }
            }
          };

          this._disableRowLoaders();
          this.props.onPress(currentLocation, currentLocation);
        } else {
          this._requestNearby(position.coords.latitude, position.coords.longitude);
        }
      },
      () => {
        this._disableRowLoaders();
      },
      options
    );
  }

  _onPress = async (rowData) => {
    if (rowData.isPredefinedPlace !== true && this.props.fetchDetails === true) {
      if (rowData.isLoading === true) {
        // already requesting
        return;
      }
      // display loader
      this._enableRowLoader(rowData);

      // fetch details
      let responseJSON = {};
      try {
        responseJSON = await this.props.client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
          placeid: rowData.place_id,
          language: 'en'
        }).timeoutWithError(5000, 'Place request timed out');
        const details = responseJSON.result;
        this._disableRowLoaders();
        this._onBlur();

        this.setState({
          text: this._renderDescription( rowData ),
        });

        delete rowData.isLoading;
        this.props.onPress(rowData, details);
      } catch (error){
        Logger.warning(error);
        this._disableRowLoaders();
        if (this.props.autoFillOnNotFound) {
          this.setState({
            text: this._renderDescription(rowData)
          });
          delete rowData.isLoading;
        }

        if (!this.props.onNotFound) {
          console.warn('google places autocomplete: ' + responseJSON.status);
        } else {
          this.props.onNotFound(responseJSON);
        }
      }
    } else if (rowData.isCurrentLocation === true) {
      // display loader
      this._enableRowLoader(rowData);

      this.setState({
        text: this._renderDescription( rowData ),
      });

      this.triggerBlur(); // hide keyboard but not the results
      delete rowData.isLoading;
      this.getCurrentLocation();
    } else {
      this.setState({
        text: this._renderDescription( rowData ),
      });

      this._onBlur();
      delete rowData.isLoading;
      const predefinedPlace = this._getPredefinedPlace(rowData);

      // sending predefinedPlace as details for predefined places
      this.props.onPress(predefinedPlace, predefinedPlace);
    }
  }

  _enableRowLoader = (rowData) => {
    const rows = this.buildRowsFromResults(this._results);
    for (let i = 0; i < rows.length; i++) {
      if ((rows[i].place_id === rowData.place_id) || (rows[i].isCurrentLocation === true && rowData.isCurrentLocation === true)) {
        rows[i].isLoading = true;
        this.setState({
          dataSource: rows,
        });
        break;
      }
    }
  }

  _disableRowLoaders = () => {
    for (let i = 0; i < this._results.length; i++) {
      if (this._results[i].isLoading === true) {
        this._results[i].isLoading = false;
      }
    }

    this.setState({
      dataSource: this.buildRowsFromResults(this._results),
    });
  }

  _getPredefinedPlace = (rowData) => {
    if (rowData.isPredefinedPlace !== true) {
      return rowData;
    }

    for (let i = 0; i < this.props.predefinedPlaces.length; i++) {
      if (this.props.predefinedPlaces[i].description === rowData.description) {
        return this.props.predefinedPlaces[i];
      }
    }

    return rowData;
  }

  _requestNearby = async (latitude, longitude) => {
    if (latitude !== undefined && longitude !== undefined && latitude !== null && longitude !== null) {
      try {
        const responseJSON = await this.props.client.invokeJSONCommand('mapsController', 'requestNearbyPlaces', {
          latitude, longitude,
          rankby: 'distance'
        });
        this._disableRowLoaders();
        this.setState({
          dataSource: this.buildRowsFromResults(responseJSON.results),
        });
      } catch (error) {
        console.warn('google places autocomplete: ' + error);
      }
    } else {
      this._results = [];
      this.setState({
        dataSource: this.buildRowsFromResults([]),
      });
    }
  }

  _request = async (text) => {
    if (text.length >= this.props.minLength) {
      try {
        const responseJSON = await this.props.client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {
          input: text,
          language: 'en'
        });
        this._results = responseJSON.predictions;
        this.setState({
          dataSource: this.buildRowsFromResults(responseJSON.predictions),
        });
      } catch (error) {
        console.warn('google places autocomplete: ' + error);
      }
    } else {
      this._results = [];
      this.setState({
        dataSource: this.buildRowsFromResults([]),
      });
    }
  }

  _onChangeText = (text) => {
    this._request(text);
    this.setState({
      text
    });
  }

  _handleChangeText = (text) => {
    this._onChangeText(text);

    const onChangeText = this.props && this.props.onChangeText;

    if (onChangeText) {
      onChangeText(text);
    }
  }

  _getRowLoader() {
    return (
      <ActivityIndicator
        animating={true}
        size={0}
      />
    );
  }

  _renderRowData = (rowData) => {
    if (this.props.renderRow) {
      return this.props.renderRow(rowData);
    }

    return (
      <Text style={[{flex: 1}, defaultStyles.description, this.props.styles.description, rowData.isPredefinedPlace ? this.props.styles.predefinedPlacesDescription : {}]} numberOfLines={1}>
        {this._renderDescription(rowData)}
      </Text>
    );
  }

  _renderDescription = (rowData) => {
    if (this.props.renderDescription) {
      return this.props.renderDescription(rowData);
    }

    return rowData.description || rowData.formatted_address || rowData.name;
  }

  _renderLoader = (rowData) => {
    if (rowData.isLoading === true) {
      return (
        <View style={[defaultStyles.loader, this.props.styles.loader]}>
          {this._getRowLoader()}
        </View>
      );
    }

    return null;
  }

  _renderRow = (rowData = {}) => {
    return (
      <ScrollView
        style={{ flex: 1 }}
        scrollEnabled={this.props.isRowScrollable}
        keyboardShouldPersistTaps={this.props.keyboardShouldPersistTaps}
        horizontal={true}
        showsHorizontalScrollIndicator={false}
        showsVerticalScrollIndicator={false}>
        <TouchableHighlight
          style={{ width: WINDOW.width }}
          onPress={() => this._onPress(rowData)}
          underlayColor={this.props.listUnderlayColor || '#c8c7cc'}
        >
          <View style={[defaultStyles.row, this.props.styles.row, rowData.isPredefinedPlace ? this.props.styles.specialItemRow : {}]}>
            {this._renderRowData(rowData)}
            {this._renderLoader(rowData)}
          </View>
        </TouchableHighlight>
      </ScrollView>
    );
  }

  _renderSeparator = (sectionID, rowID) => {
    if (rowID == this.state.dataSource.length - 1) {
      return null;
    }

    return (
      <View
        key={ `${sectionID}-${rowID}` }
        style={[defaultStyles.separator, this.props.styles.separator]} />
    );
  }

  _onBlur = () => {
    this.triggerBlur();

    this.setState({
      listViewDisplayed: false
    });
  }

  _onFocus = () => this.setState({ listViewDisplayed: true })

  _renderPoweredLogo = () => {
    return null;
  }

  _shouldShowPoweredLogo = () => {
    if (!this.props.enablePoweredByContainer || this.state.dataSource.length == 0) {
      return false;
    }

    for (let i = 0; i < this.state.dataSource.length; i++) {
      const row = this.state.dataSource[i];

      if (!row.hasOwnProperty('isCurrentLocation') && !row.hasOwnProperty('isPredefinedPlace')) {
        return true;
      }
    }

    return false;
  }

  _renderLeftButton = () => {
    if (this.props.renderLeftButton) {
      return this.props.renderLeftButton();
    }
  }

  _renderRightButton = () => {
    if (this.props.renderRightButton) {
      return this.props.renderRightButton();
    }
  }

  _getFlatList = () => {
    const keyGenerator = () => (
      Math.random().toString(36).substr(2, 10)
    );

    if ((this.state.text !== '' || this.props.predefinedPlaces.length || this.props.currentLocation === true) && this.state.listViewDisplayed === true) {
      return (
        <FlatList
          style={[defaultStyles.listView, this.props.styles.listView]}
          data={this.state.dataSource}
          keyExtractor={keyGenerator}
          extraData={[this.state.dataSource, this.props]}
          ItemSeparatorComponent={this._renderSeparator}
          renderItem={({ item }) => this._renderRow(item)}
          ListFooterComponent={this._renderPoweredLogo}
          {...this.props}
        />
      );
    }

    return null;
  }
  render() {
    const {
      onFocus,
      ...userProps
    } = this.props.textInputProps;

    const listStyle = this.state.listViewDisplayed ? {flex: 1} : {};

    return (
      <View
        style={[defaultStyles.container, this.props.styles.container, listStyle]}
      >
        {!this.props.textInputHide &&
          <View
            style={[defaultStyles.textInputContainer, this.props.styles.textInputContainer]}
          >
            {this._renderLeftButton()}
            <TextInput
              ref="textInput"
              returnKeyType={this.props.returnKeyType}
              autoFocus={this.props.autoFocus}
              autoCorrect={false}
              style={[defaultStyles.textInput, this.props.styles.textInput]}
              value={this.state.text}
              placeholder={this.props.placeholder}

              placeholderTextColor={this.props.placeholderTextColor}
              onFocus={onFocus ? () => {this._onFocus(); onFocus();} : this._onFocus}
              onBlur={() => this._onBlur()}
              clearButtonMode="while-editing"
              underlineColorAndroid={this.props.underlineColorAndroid}
              { ...userProps }
              onChangeText={this._handleChangeText}
            />
            {this._renderRightButton()}
          </View>
        }
        {this._getFlatList()}
        {this.props.children}
      </View>
    );
  }
}

PlacesAutocomplete.propTypes = {
  placeholder: PropTypes.string,
  placeholderTextColor: PropTypes.string,
  underlineColorAndroid: PropTypes.string,
  returnKeyType: PropTypes.string,
  onPress: PropTypes.func,
  onNotFound: PropTypes.func,
  onFail: PropTypes.func,
  minLength: PropTypes.number,
  fetchDetails: PropTypes.bool,
  autoFocus: PropTypes.bool,
  autoFillOnNotFound: PropTypes.bool,
  getDefaultValue: PropTypes.func,
  timeout: PropTypes.number,
  onTimeout: PropTypes.func,
  styles: PropTypes.object,
  textInputProps: PropTypes.object,
  enablePoweredByContainer: PropTypes.bool,
  predefinedPlaces: PropTypes.array,
  currentLocation: PropTypes.bool,
  currentLocationLabel: PropTypes.string,
  enableHighAccuracyLocation: PropTypes.bool,
  predefinedPlacesAlwaysVisible: PropTypes.bool,
  enableEmptySections: PropTypes.bool,
  renderDescription: PropTypes.func,
  renderRow: PropTypes.func,
  renderLeftButton: PropTypes.func,
  renderRightButton: PropTypes.func,
  listUnderlayColor: PropTypes.string,
  debounce: PropTypes.number,
  isRowScrollable: PropTypes.bool,
  text: PropTypes.string,
  textInputHide: PropTypes.bool
};
PlacesAutocomplete.defaultProps = {
  placeholder: 'Search',
  placeholderTextColor: '#A8A8A8',
  isRowScrollable: true,
  underlineColorAndroid: 'transparent',
  returnKeyType: 'default',
  onPress: () => {},
  onNotFound: () => {},
  minLength: 0,
  fetchDetails: false,
  autoFocus: false,
  autoFillOnNotFound: false,
  keyboardShouldPersistTaps: 'always',
  getDefaultValue: () => '',
  timeout: 20000,
  onTimeout: () => console.warn('google places autocomplete: request timeout'),
  styles: {},
  textInputProps: {},
  enablePoweredByContainer: true,
  predefinedPlaces: [],
  currentLocation: false,
  currentLocationLabel: 'Current location',
  enableHighAccuracyLocation: true,
  predefinedPlacesAlwaysVisible: false,
  enableEmptySections: true,
  listViewDisplayed: 'auto',
  debounce: 0,
  textInputHide: false
};
