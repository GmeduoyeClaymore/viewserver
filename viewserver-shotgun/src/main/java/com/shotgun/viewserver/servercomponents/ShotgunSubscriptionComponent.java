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
        authenticationHandlerRegistry.register("compatableVersion", new CompatableVersionAuthenticationCommand(basicServerComponents.getServerCatalog(),versionInfo));
        authenticationHandlerRegistry.register("compatableVersionEvenlyDistributed", new CompatableVersionEvenlyDistributedAuthenticationCommand(basicServerComponents.getServerCatalog(),versionInfo));
    }

}

