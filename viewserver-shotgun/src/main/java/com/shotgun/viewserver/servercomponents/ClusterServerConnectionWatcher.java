package com.shotgun.viewserver.servercomponents;

import io.viewserver.client.ViewServerClient;
import io.viewserver.network.ReconnectionSettings;
import rx.Observable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ClusterServerConnectionWatcher {

    String peerUrl;
    private ClientVersionInfo versionInfo;
    private ViewServerClient client;

    public ClusterServerConnectionWatcher(String peerUrl, ClientVersionInfo versionInfo) {
        this.peerUrl = peerUrl;
        this.versionInfo = versionInfo;
    }

    public Observable waitForDeath(){
        return getOrCreateClient().getNetwork().disconnectionObservable();
    }

    private ViewServerClient getOrCreateClient() {
        try {
        if(client == null) {
            client = new ViewServerClient("serverConnectionWatcher_" + versionInfo.getServerEndPoint(), peerUrl, ReconnectionSettings.Times(100));
        }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    public void close(){
        if(client != null){
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
