import Logger from 'common/Logger';

export const fetchRoute = async  (client, origin, destination) => {
  const mode = 'driving';
  const json = await client.invokeJSONCommand('mapsController', 'mapDirectionRequest', {origin: `${origin.latitude},${origin.longitude}`, destination: `${destination.latitude},${destination.longitude}`, mode});
  if (json.routes.length){
    const route = json.routes[0];
    return {
      distance: route.legs.reduce((carry, curr) => {
        return carry + curr.distance.value;
      }, 0) / 1000,
      duration: route.legs.reduce((carry, curr) => {
        return carry + curr.duration.value;
      }, 0) / 60,
      coordinates: decode(route.overview_polyline.points)
    };
  }
  throw new Error('No routes returned');
};

export const getAddressText = (address) => {
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
};

export const parseGooglePlacesData = (details) => {
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
};

const decode = (t, e) => {
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
};
