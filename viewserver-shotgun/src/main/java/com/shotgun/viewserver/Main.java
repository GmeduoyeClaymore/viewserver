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
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        ViewServerLauncher launcher = new ViewServerLauncher<IViewServerConfiguration>(
                new XmlViewServerConfiguration(System.getProperty("viewserver.configurationFile", "config.xml")),
                DemoViewServerMaster::new);
        launcher.launch(args);
    }
}
