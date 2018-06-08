package com.shotgun.viewserver.servercomponents;

public class ClientVersionInfo {
    private String serverEndPoint;
    private String compatableClientVersion;

    public ClientVersionInfo(String serverEndPoint, String compatableClientVersion) {
        this.serverEndPoint = serverEndPoint;
        this.compatableClientVersion = compatableClientVersion;
    }

    public String getServerEndPoint() {
        return serverEndPoint;
    }

    public String getCompatableClientVersion() {
        return compatableClientVersion;
    }
}
