package features;

import cucumber.runtime.java.picocontainer.PicoFactory;
import io.viewserver.server.steps.IViewServerContext;
import io.viewserver.server.steps.InProcessViewServerContext;

public class CustomPicoFactory extends PicoFactory {
    public CustomPicoFactory() {
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if(type.isAssignableFrom(IViewServerContext.class)){
            return (T) new ShotgunInProcViewServerContext();
        }
        return super.getInstance(type);
    }
}
