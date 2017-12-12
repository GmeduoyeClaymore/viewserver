package com.shotgun.viewserver;

import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;

@Controller(name = "paymentController")
public class PaymentController{

    @ControllerAction(path = "process")
    public PaymentResponse processPayment(PaymentCredentials credentials){
        if(credentials.name.equals("error")){
            throw new RuntimeException("Error caused by parameter name !!!!");
        }
        return new PaymentResponse("myDemoResponse" + credentials.name,credentials.address);
    }

}
