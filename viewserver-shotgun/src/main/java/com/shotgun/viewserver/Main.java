package com.shotgun.viewserver;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String environment = System.getProperty("shotgun.environment", "mock");
        ShotgunServerLauncher launcher = new ShotgunServerLauncher();
        launcher.run(environment,args.length > 0 && args[0].equals("bootstrap"), true, false).take(1).toBlocking().first();
        new CountDownLatch(1).await();
    }
}

