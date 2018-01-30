import FirebaseConstants from './FirebaseConstants';
import { Alert } from 'react-native';

const API_URL = 'https://fcm.googleapis.com/fcm/send';

class FirebaseClient {
  async send(body) {
    if (FirebaseClient.KEY === 'YOUR_API_KEY'){
      Alert.alert('Set your API_KEY in app/FirebaseConstants.js');
      return;
    }
  	const headers = new Headers({
  		'Content-Type': 'application/json',
      'Authorization': 'key=' + FirebaseConstants.KEY
  	});

    try {
      let response = await fetch(API_URL, { method: 'POST', headers, body });
      try {
        response = await response.json();
        if (!response.success){
          Alert.alert('Failed to send notification, check error log xx' + JSON.stringify(response));
        }
      } catch (err){
        Alert.alert('Failed to send notification, check error log ' + JSON.stringify(err));
      }
    } catch (err) {
      Alert.alert(err && err.message);
    }
  }
}

const firebaseClient = new FirebaseClient();
export default firebaseClient;
