package com.shotgun.viewserver;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
        String environment = System.getProperty("shotgun.environment", "mock");
        ShotgunServerLauncher launcher = new ShotgunServerLauncher();
        launcher.run(environment,args.length > 0 && args[0].equals("bootstrap"));
    }
}

