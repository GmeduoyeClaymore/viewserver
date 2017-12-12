package com.shotgun.viewserver;

import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;

@Controller(name = "paymentController")
public class PaymentController{

    @ControllerAction(path = "process")
    public String processPayment(PaymentCredentials credentials){
        if(credentials.name.equals("error")){
            throw new RuntimeException("Error caused by parameter name !!!!");
        }
        return "myDemoResponse" + credentials.name + "" + credentials.address;
    }

}
