package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.IDatabaseUpdater;
import io.viewserver.server.components.IBasicServerComponents;

public class MockShotgunControllersComponent extends ShotgunControllersComponent{

    public MockShotgunControllersComponent(IBasicServerComponents basicServerComponents, IDatabaseUpdater databaseUpdater) {
        super(basicServerComponents, databaseUpdater);
    }
}
