package com.shotgun.viewserver;

import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.core.ExecutionContext;

@Controller(name = "paymentController")
public class PaymentController{

    //TODO example of injecting server execution context for our async commands
    private ExecutionContext serverExecutionContext;

    public PaymentController(ExecutionContext serverExecutionContext) {

        this.serverExecutionContext = serverExecutionContext;
    }

    @ControllerAction(path = "process", isSynchronous = true)
    public PaymentResponse processPayment(PaymentCredentials credentials){
        if(credentials.name.equals("error")){
            throw new RuntimeException("Error caused by parameter name !!!!");
        }
        //TODO do whatever you want here we are on the correct thread
        return new PaymentResponse("SUCCESS synch processResponse" + credentials.name,credentials.address);
    }

    @ControllerAction(path = "reject",isSynchronous = false)
    public PaymentResponse rejectPayment(PaymentCredentials credentials){
        if(credentials.name.equals("error")){
            throw new RuntimeException("Error caused by parameter name !!!!");
        }
        //TODO careful that you don't try to modify any operators from a background thread
        //serverExecutionContext.getReactor().scheduleTask();
        return new PaymentResponse("SUCCESS asynchronous rejectResponse" + credentials.name,credentials.address);
    }

}
