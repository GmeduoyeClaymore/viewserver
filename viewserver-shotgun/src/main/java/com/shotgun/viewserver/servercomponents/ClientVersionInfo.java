package com.shotgun.viewserver.servercomponents;

public class ClientVersionInfo {
    private String serverEndPoint;
    private String compatibleClientVersion;

    public ClientVersionInfo(String serverEndPoint, String compatibleClientVersion) {
        this.serverEndPoint = serverEndPoint;
        this.compatibleClientVersion = compatibleClientVersion;
    }

    public String getServerEndPoint() {
        return serverEndPoint;
    }

    public String getCompatibleClientVersion() {
        return compatibleClientVersion;
    }
}
