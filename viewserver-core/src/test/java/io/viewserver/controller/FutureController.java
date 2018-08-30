package io.viewserver.controller;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

@Controller(name = "futureController")
public class FutureController{

    private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
    Integer current = 0;

    @ControllerAction(path = "plus")
    public ListenableFuture<Integer> plus(Integer addition){
        return service.submit(() -> current+=addition);
    }

    @ControllerAction(path = "plusIntercepted", interceptor = FutureLoggerInterceptor.class)
    public ListenableFuture<Integer> plusIntercepted(Integer addition){
        return service.submit(() -> current+=addition);
    }

    @ControllerAction(path = "plusOne")
    public ListenableFuture<Integer> plusone(){
        return service.submit(() -> current+=1);
    }

    @ControllerAction(path = "plusOneIntercepted", interceptor = FutureLoggerInterceptor.class)
    public ListenableFuture<Integer> plusOneIntercepted(){
        return service.submit(() -> current+=1);
    }

    @ControllerAction(path = "plusGenericIntercepted", interceptor = GenericFutureLoggerInterceptor.class)
    public ListenableFuture<Integer> plusGenericIntercepted(Integer addition){
        return service.submit(() -> current+=addition);
    }
    @ControllerAction(path = "plusOneGenericIntercepted", interceptor = GenericFutureLoggerInterceptor.class)
    public ListenableFuture<Integer> plusOneGenericIntercepted(){
        return service.submit(() -> current+=1);
    }

}
