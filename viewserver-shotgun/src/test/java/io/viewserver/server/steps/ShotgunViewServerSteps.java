package io.viewserver.server.steps;

import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.java.en.Given;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ShotgunViewServerSteps {

    ShotgunServerLauncher launcher = new ShotgunServerLauncher();

    public ShotgunViewServerSteps() {
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                launcher.run("it", true);
                launcher.run("it", false);
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        latch.await(10, TimeUnit.SECONDS);

    }
}
