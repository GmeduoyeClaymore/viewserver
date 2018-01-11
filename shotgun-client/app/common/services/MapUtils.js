import Logger from 'common/Logger';

export default class MapUtils {
  static getAddressText(address){
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
      return result.join(',');
  }

  static parseGooglePlacesData(details){
    try {
      const {name, place_id, geometry, address_components} = details;
      const {location} = geometry;
      const country = address_components.find(c => c.types.includes('country'));
      const city = address_components.find(c => c.types.includes('postal_town'));
      const postCode = address_components.find(c => c.types.includes('postal_code'));

      return  {
        line1: name,
        city: city !== undefined ? city.long_name : undefined,
        postCode: postCode !== undefined ? postCode.long_name : undefined,
        country: country !== undefined ? country.long_name : undefined,
        googlePlaceId: place_id,
        latitude: location.lat,
        longitude: location.lng
      };
    } catch (err) {
      Logger.error(err);
    }
  }
}
