package io.viewserver.server.steps;

import com.shotgun.viewserver.PropertyUtils;
import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ShotgunViewServerSteps {

    private ViewServerClientContext clientContext;
    private static int runCount = 0;
    private HashMap<String,ShotgunServerLauncher> launcherHashMap = new HashMap<>();

    public ShotgunViewServerSteps(ViewServerClientContext clientContext) {
        this.clientContext = clientContext;
    }


    @After
    public void afterScenario() {
        launcherHashMap.values().stream().forEach(lau -> lau.stop());
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws InterruptedException {
        runViewServer(true);
    }

    @Given("^a running shotgun viewserver with url \"([^\"]*)\" and version \"([^\"]*)\" and bootstrap \"([^\"]*)\"$")
    public void a_running_shotgun_viewserver_with_url(String url,String version,String bootstrap) throws InterruptedException {
        System.setProperty("server.isMaster",bootstrap);
        System.setProperty("server.endpoint",url);
        System.setProperty("server.name",url);
        System.setProperty("server.version",version);
        runViewServer(Boolean.parseBoolean(bootstrap));
    }

    private void runViewServer(Boolean bootstrap) {
        clientContext.closeClients();
        PropertyUtils.loadProperties("cucumber");
        String env = System.getProperty("env");
        PropertyUtils.loadProperties(env);
        String endPoint =  System.getProperty("server.endpoint");
        killIfAlive(endPoint);
        if(Boolean.parseBoolean(System.getProperty("serverShouldBeStarted", "true"))) {
            ShotgunServerLauncher launcher = new ShotgunServerLauncher();
            launcher.run(env, bootstrap, 0==runCount++);
            launcherHashMap.put(endPoint,launcher);
        }
    }

    private void killIfAlive(String endPoint) {
        ShotgunServerLauncher launcherForUrl = launcherHashMap.get(endPoint);
        if(launcherForUrl != null) {
            launcherForUrl.stop();
            launcherHashMap.put(endPoint,null);
        }
    }

    @And("^Shotgun viewserver with url \"([^\"]*)\" is killed$")
    public void shotgunViewserverWithUrlIsKilled(String serverUrl) throws Throwable {
        killIfAlive(serverUrl);
    }
}
