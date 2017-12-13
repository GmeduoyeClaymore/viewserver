package io.viewserver.command;

import io.viewserver.network.IPeerSession;

/**
 * Created by Gbemiga on 13/12/17.
 */
public class ControllerContext implements AutoCloseable{
    private IPeerSession peerSession;
    private static ThreadLocal<ControllerContext> current = new ThreadLocal<>();

    private ControllerContext(IPeerSession peerSession) {
        this.peerSession = peerSession;
    }

    public IPeerSession getPeerSession() {
        return peerSession;
    }

    public static ControllerContext create(IPeerSession session){
        return create(new ControllerContext(session));
    }

    public static ControllerContext create(ControllerContext context){
        current.set(context);
        return context;
    }

    public static ControllerContext Current(){
        return current.get();
    }

    @Override
    public void close() throws Exception {
        current.set(null);
    }
}
