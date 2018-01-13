package io.viewserver.command;

import io.viewserver.network.IPeerSession;
import io.viewserver.network.PeerSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gbemiga on 13/12/17.
 */


public class ControllerContext implements AutoCloseable{
    private IPeerSession peerSession;
    private static ThreadLocal<ControllerContext> current = new ThreadLocal<>();
    private static ConcurrentHashMap<IPeerSession,ConcurrentHashMap<String,Object>> contextParams = new ConcurrentHashMap<>();

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

    private static ConcurrentHashMap<String,Object> getParams(){
        return contextParams.get(current.get().getPeerSession());
    }

    public static void set(String name, Object value) {
        IPeerSession peerSession1 = current.get().getPeerSession();
        synchronized (peerSession1){
            ConcurrentHashMap<String,Object> params = getParams();
            if(params == null){
                params = new ConcurrentHashMap<>();
                peerSession1.addDisconnectionHandler(new PeerSession.IDisconnectionHandler() {
                    @Override
                    public void handleDisconnect(IPeerSession peerSession) {
                        try{
                            contextParams.remove(peerSession1);
                        }
                        finally {
                            peerSession1.removeDisconnectionHandler(this);
                        }
                    }
                });
                contextParams.put(peerSession1, params);
            }
            params.put(name,value);
        }
    }

    public static Object get(String name) {
        synchronized (current.get().getPeerSession()){
            ConcurrentHashMap<String,Object> params = getParams();
            if(params == null){
                return null;
            }
            return params.get(name);
        }
    }
}
