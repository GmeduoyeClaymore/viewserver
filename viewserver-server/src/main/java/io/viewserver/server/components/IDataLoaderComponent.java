package io.viewserver.server.components;

public interface IDataLoaderComponent extends IServerComponent{
    enum StartupStrategy{
        WhenOperatorsReady,
        WhenDataLoadersReady
    }

    default StartupStrategy getStartupStrategy(){
        return StartupStrategy.WhenDataLoadersReady;
    }
}
