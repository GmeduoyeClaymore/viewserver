package com.shotgun.viewserver.servercomponents;

import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.server.components.BasicSubscriptionComponent;
import io.viewserver.server.components.IBasicServerComponents;

public class ShotgunSubscriptionComponent extends BasicSubscriptionComponent {

    private IBasicServerComponents basicServerComponents;
    private ClientVersionInfo versionInfo;

    public ShotgunSubscriptionComponent(IBasicServerComponents basicServerComponents, ClientVersionInfo versionInfo) {
        super(basicServerComponents);
        this.basicServerComponents = basicServerComponents;
        this.versionInfo = versionInfo;
    }

    protected void registerAuthenticationHandlers() {
        authenticationHandlerRegistry.register("compatibleVersion", new CompatibleVersionAuthenticationCommand(basicServerComponents.getServerCatalog(),versionInfo));
        authenticationHandlerRegistry.register("compatibleVersionEvenlyDistributed", new CompatibleVersionEvenlyDistributedAuthenticationCommand(basicServerComponents.getServerCatalog(),versionInfo));
    }

}

