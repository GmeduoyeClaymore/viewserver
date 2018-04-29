package io.viewserver.server.steps;

import com.shotgun.viewserver.PropertyUtils;
import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ShotgunViewServerSteps {

    private ViewServerClientContext clientContext;
    private ShotgunServerLauncher launcher;

    public ShotgunViewServerSteps(ViewServerClientContext clientContext, ShotgunServerLauncher launcher) {
        this.clientContext = clientContext;
        this.launcher = launcher;
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws InterruptedException {
        clientContext.closeClients();
        PropertyUtils.loadProperties("cucumber");
        String env = System.getProperty("env");
        PropertyUtils.loadProperties(env);
        launcher.stop();
        if(Boolean.parseBoolean(System.getProperty("serverShouldBeStarted", "true"))) {
            launcher.run(env, true);
            launcher.run(env, false);
        }
    }

}
