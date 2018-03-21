package com.shotgun.viewserver;

import io.viewserver.server.ViewServerLauncher;

import java.io.IOException;

/**
 * Created by nick on 24/02/2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ViewServerLauncher launcher = new ViewServerLauncher<IShotgunViewServerConfiguration>(
                new ShotgunViewServerConfiguration(System.getenv("viewserver.configurationFile")),
                ShotgunViewServerMaster::new);
        launcher.launch(args);
    }
}
