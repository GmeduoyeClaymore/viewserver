package io.viewserver.network;

public class ReconnectionSettings{
    private boolean shouldReconnect;
    private int noFailures;

    public ReconnectionSettings(boolean shouldReconnect, int noFailures) {
        this.shouldReconnect = shouldReconnect;
        this.noFailures = noFailures;
    }

    public boolean isShouldReconnect() {
        return shouldReconnect;
    }

    public int getNoFailures() {
        return noFailures;
    }

    public static ReconnectionSettings Dont = new ReconnectionSettings(false,0);
    public static ReconnectionSettings Forever = new ReconnectionSettings(true,Integer.MAX_VALUE);
    public static ReconnectionSettings Times(int noFailures){
        return  new ReconnectionSettings(true,noFailures);
    };
}
