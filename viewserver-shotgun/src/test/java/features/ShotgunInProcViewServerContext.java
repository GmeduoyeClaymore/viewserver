package features;

import com.shotgun.viewserver.ShotgunViewServerMaster;
import com.shotgun.viewserver.setup.ShotgunBootstrapper;
import io.viewserver.datasource.DataSource;
import io.viewserver.network.EndpointFactoryRegistry;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.IViewServerMasterConfiguration;
import io.viewserver.server.ViewServerMaster;
import io.viewserver.server.ViewServerMasterBase;
import io.viewserver.server.ViewServerSlave;
import io.viewserver.server.steps.IViewServerContext;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShotgunInProcViewServerContext implements IViewServerContext {
    public ShotgunViewServerMaster master;
    public String bootstrapperClass = ShotgunBootstrapper.class.getName();
    public IViewServerMasterConfiguration masterConfiguration = new IViewServerMasterConfiguration() {
        @Override
        public String getBootstrapperClass() {
            return bootstrapperClass;
        }

        @Override
        public Iterable<IEndpoint> getMasterEndpoints() {
            try {
                return Collections.singleton(
                        EndpointFactoryRegistry.createEndpoint("ws://127.0.0.1:8080")
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getMasterDatabasePath() {
            return System.getProperty("user.home") + "/viewserver_test";
        }
    };
    private DataSource dataSource;

    public String getUrl() {
        return "inproc://master";
    }

    @Override
    public ViewServerMasterBase getMaster() {
        return master;
    }

    @Override
    public List<ViewServerSlave> getSlaves() {
        return new ArrayList<>();
    }

    @Override
    public void setMaster(ViewServerMasterBase master) {
        this.master = (ShotgunViewServerMaster) master;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getBootstrapperClass() {
        return bootstrapperClass;
    }

    @Override
    public void setBootstrapperClass(String bootstrapperClass) {
        this.bootstrapperClass = bootstrapperClass;
    }

    @Override
    public IViewServerMasterConfiguration getMasterConfiguration() {
        return masterConfiguration;
    }
}

