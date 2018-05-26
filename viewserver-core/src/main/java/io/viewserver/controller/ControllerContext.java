package io.viewserver.controller;

import io.viewserver.network.IPeerSession;
import io.viewserver.network.PeerSession;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

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

    public static Scheduler Scheduler(ControllerContext context){
        return Schedulers.from(
                command -> {
                    try{
                        ControllerContext.create(context);
                        command.run();
                    }finally {
                        ControllerContext.closeStatic();
                    }
                }
        );
    }

    public static ControllerContext Current(){
        return current.get();
    }

    @Override
    public void close(){
        current.set(null);
    }

    public static void closeStatic(){
        current.set(null);
    }


    private static Set<String> getParamNames(){
        IPeerSession peerSession = current.get().getPeerSession();
        return getParamNames(peerSession);
    }

    public static Set<String> getParamNames(IPeerSession peerSession) {
        ConcurrentHashMap<String, Object> stringObjectConcurrentHashMap = contextParams.get(peerSession);
        if(stringObjectConcurrentHashMap == null){
            return Collections.emptySet();
        }
        return stringObjectConcurrentHashMap.keySet();
    }

    public static void set(String name, Object value) {
        IPeerSession peerSession1 = current.get().getPeerSession();
        set(name, value, peerSession1);
    }

    public static void setFactory(String name, Callable value, IPeerSession peerSession1) {
        set(name,value, peerSession1);
    }
    public static void set(String name, Object value, IPeerSession peerSession1) {
        synchronized (peerSession1){
            ConcurrentHashMap<String,Object> params = contextParams.get(peerSession1);
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

    public static Object get(String name, IPeerSession peerSession) {
        synchronized (peerSession){
            ConcurrentHashMap<String,Object> params = contextParams.get(peerSession);
            if(params == null){
                return null;
            }
            Object o = params.get(name);
            if( o instanceof Callable){
                try {
                    return ((Callable)o).call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return o;
        }
    }

    public static Object getStatic(String name, IPeerSession peerSession) {
        synchronized (peerSession){
            ConcurrentHashMap<String,Object> params = contextParams.get(peerSession);
            if(params == null){
                return null;
            }
            Object o = params.get(name);
            if( o instanceof Callable){
                if("now".equals(name)){
                    try {
                        return ((Callable)o).call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
            return o;
        }
    }

    public static Throwable Unwrap(Throwable e){
        if(e instanceof InvocationTargetException){
            return ((InvocationTargetException)e).getTargetException();
        }
        return e;
    }
}
