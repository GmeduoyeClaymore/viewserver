package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class FirebaseConnectionFactory{
    private String firebaseKeyPath;
    private static Firestore connection;

    public FirebaseConnectionFactory(String firebaseKeyPath) {
        this.firebaseKeyPath = firebaseKeyPath;
    }

    private void createConnection() {
        try {
            URL resource = getClass().getClassLoader().getResource(firebaseKeyPath);
            File firebaseKey = new File(resource.getFile());

            InputStream serviceAccount = new FileInputStream(firebaseKey);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setProjectId("shotgundelivery-188314")
                    .build();
            FirebaseApp.initializeApp(options);

            connection = FirestoreClient.getFirestore();
        }catch(Exception ex){
            throw new RuntimeException("Could not open a connection to the firebase database");
        }
    }

    @JsonIgnore
    public Firestore getConnection(){
        if(connection == null){
            createConnection();
        }
        return connection;
    }

    public String getFirebaseKeyPath() {
        return firebaseKeyPath;
    }

    public void setFirebaseKeyPath(String firebaseKeyPath) {
        this.firebaseKeyPath = firebaseKeyPath;
    }
}
