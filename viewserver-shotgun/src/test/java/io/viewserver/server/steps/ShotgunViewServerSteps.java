package io.viewserver.server.steps;

import com.shotgun.viewserver.ShotgunViewServerConfiguration;
import com.shotgun.viewserver.ShotgunViewServerMaster;
import cucumber.api.java.en.Given;
import io.viewserver.network.netty.inproc.NettyInProcEndpoint;
import io.viewserver.server.ViewServerMasterTest;
import io.viewserver.server.ViewServerSlave;
import io.viewserver.server.setup.BootstrapperBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShotgunViewServerSteps {

    private IViewServerContext viewServerContext;
    private static final Logger log = LoggerFactory.getLogger(ViewServerSteps.class);
    public ShotgunViewServerSteps(IViewServerContext viewServerContext) {
        this.viewServerContext = viewServerContext;
    }

    @Given("^a running shotgun viewserver$")
    public void a_running_shotgun_viewserver() throws Throwable {
//        System.setProperty("server.bypassDataSources", "true");
        BootstrapperBase.bootstrap(viewServerContext.getMasterConfiguration());

        viewServerContext.setMaster(new ShotgunViewServerMaster("master", new ShotgunViewServerConfiguration("config-integration-test.xml")));

        viewServerContext.getMaster().run();

    }
}
