package io.viewserver.server.steps;

import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.java.en.Given;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;

public class ShotgunViewServerSteps {

    public ShotgunViewServerSteps() {
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws Throwable {
        ShotgunServerLauncher launcher = new ShotgunServerLauncher();
        launcher.run("it", true);
        launcher.run("it", false);

    }
}
