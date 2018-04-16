package io.viewserver.server.steps;

import cucumber.runtime.java.picocontainer.PicoFactory;

public class CustomPicoFactory extends PicoFactory {
    public CustomPicoFactory() {
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if(type.isAssignableFrom(IViewServerContext.class)){
            return (T) new InProcessViewServerContext();
        }
        return super.getInstance(type);
    }
}
