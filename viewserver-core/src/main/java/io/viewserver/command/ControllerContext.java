package io.viewserver.command;

import io.viewserver.network.IPeerSession;
import io.viewserver.network.PeerSession;

import java.lang.reflect.InvocationTargetException;
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
        IPeerSession peerSession = current.get().getPeerSession();
        return getParams(peerSession);
    }

    public static ConcurrentHashMap<String, Object> getParams(IPeerSession peerSession) {
        return contextParams.get(peerSession);
    }

    public static void set(String name, Object value) {
        IPeerSession peerSession1 = current.get().getPeerSession();
        set(name, value, peerSession1);
    }

    public static void set(String name, Object value, IPeerSession peerSession1) {
        synchronized (peerSession1){
            ConcurrentHashMap<String,Object> params = getParams(peerSession1);
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
        IPeerSession peerSession = current.get().getPeerSession();
        return get(name, peerSession);
    }

    private static Object get(String name, IPeerSession peerSession) {
        synchronized (peerSession){
            ConcurrentHashMap<String,Object> params = getParams(peerSession);
            if(params == null){
                return null;
            }
            return params.get(name);
        }
    }

    public static Throwable Unwrap(Throwable e){
        if(e instanceof InvocationTargetException){
            return ((InvocationTargetException)e).getTargetException();
        }
        return e;
    }
}
