package com.shotgun.viewserver;

import io.viewserver.server.IViewServerConfiguration;
import io.viewserver.server.ViewServerLauncher;
import io.viewserver.server.XmlViewServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by nick on 24/02/2015.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ViewServerLauncher launcher = new ViewServerLauncher<IShotgunViewServerConfiguration>(
                new ShotgunViewServerConfiguration(System.getProperty("viewserver.configurationFile", "config-test.xml")),
                ShotgunViewServerMaster::new);
        launcher.launch(args);
    }
}
