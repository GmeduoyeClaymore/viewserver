package io.viewserver.server.steps;

import com.shotgun.viewserver.PropertyUtils;
import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ShotgunViewServerSteps {

    private static int runCount = 0;
    private HashMap<String,TestServer> launcherHashMap = new HashMap<>();

    public ShotgunViewServerSteps() {
    }


    @After
    public void afterScenario() {
        CountDownLatch latch = new CountDownLatch(launcherHashMap.size());
        launcherHashMap.values().stream().parallel().forEach(lau ->
                {
                    if(lau !=null){
                        try {
                            lau.stop();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }finally {
                            latch.countDown();
                        }
                    }
                }
        );
        try {
            latch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            launcherHashMap.clear();
        }
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws InterruptedException {
        if(!Boolean.parseBoolean(System.getProperty("serverShouldBeStarted", "true"))) {
            return;
        }
        TestServer server = new TestServer(null,null,true,0 == runCount++);
        PropertyUtils.loadProperties("cucumber");
        String env = System.getProperty("env");
        PropertyUtils.loadProperties(env);
        String endPoint =  System.getProperty("server.endpoint");
        killIfAlive(endPoint);
        launcherHashMap.put(endPoint,server);
        server.start();
    }

    @Given("^a running shotgun viewserver with url \"([^\"]*)\" and version \"([^\"]*)\" and bootstrap \"([^\"]*)\"$")
    public void a_running_shotgun_viewserver_with_url(String url, String version, boolean bootstrap) throws InterruptedException {
        if(!Boolean.parseBoolean(System.getProperty("serverShouldBeStarted", "true"))) {
            return;
        }
        TestServer server = new TestServer(url,version,bootstrap,0 == runCount++);
        killIfAlive(url);
        launcherHashMap.put(url,server);
        server.start();
    }

    private void killIfAlive(String endPoint) {
        TestServer launcherForUrl = launcherHashMap.get(endPoint);
        if(launcherForUrl != null) {
            launcherForUrl.stop();
            launcherHashMap.put(endPoint,null);
        }
    }

    @And("^Shotgun viewserver with url \"([^\"]*)\" is killed$")
    public void shotgunViewserverWithUrlIsKilled(String serverUrl) throws Throwable {
        killIfAlive(serverUrl);
    }

    public class TestServer implements Runnable{
        private final Thread thread;
        private final CountDownLatch started;
        private final CountDownLatch stopped;
        String url;
        String version;
        boolean bootstrap;
        boolean complete;
        boolean isStopped;
        private final Logger log = LoggerFactory.getLogger(TestServer.class);


        public TestServer(String url, String version, boolean bootstrap, boolean complete) {
            this.url = url;
            this.version = version;
            this.bootstrap = bootstrap;
            this.complete = complete;
            this.started = new CountDownLatch(1);
            this.stopped = new CountDownLatch(1);
            this.thread = new Thread(new ThreadGroup(String.format("group-%s", url)), this);
        }

        public void stop(){
            log.info("MILESTONE - Stopping server");
            this.isStopped = true;
            try {
                if (!this.stopped.await(10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Cannot detect server stopped after 10 seconds");
                }
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        public void start() throws InterruptedException {
            this.thread.start();
            if(!this.started.await(30,TimeUnit.SECONDS)){
                throw new RuntimeException("Unable to start server after 30d seconds");
            }
        }

        public void run(){
            DateTime now = DateTime.now();
            System.setProperty("server.isMaster", bootstrap + "");
            if (url != null) {
                System.setProperty("server.endpoint", url);
                System.setProperty("server.name", url + "_" + now);
            }else{
                System.clearProperty("server.endpoint");
                System.clearProperty("server.name");
            }

            if(version != null) {
                System.setProperty("server.version", version);
            }else{
                System.clearProperty("server.version");
            }
            PropertyUtils.loadProperties("cucumber");
            String env = System.getProperty("env");
            PropertyUtils.loadProperties(env);
            String endPoint = System.getProperty("server.endpoint");
            log.info("MILESTONE: Actually running view server against url {}", endPoint);
            ShotgunServerLauncher launcher = new ShotgunServerLauncher();
            launcher.run(env, bootstrap, complete).subscribe(
                    success -> {
                        this.started.countDown();
                    },
                    err -> {
                        log.error("Problem starting server",err);
                        isStopped = true;
                    }
            );

            while (!isStopped) {
                log.debug("INFO: Server is running against {}", endPoint);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                log.info("MILESTONE - Loop exited");
                launcher.stop();
                log.info("MILESTONE - Server stopped");
            }finally {
                this.stopped.countDown();
            }
        }
    }
}
