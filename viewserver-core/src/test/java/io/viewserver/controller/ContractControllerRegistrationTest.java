package io.viewserver.controller;

import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ContractControllerRegistrationTest {

    private ComposedController sut;


    @Before
    public void createSut(){
        sut = new ComposedController();
    }

    @Test
    public  void registersAllActions() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        Assert.assertEquals(3,reg.getActions().size() );
    }

    @Test
    public  void canCallMethodOnImplementedContract() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "6";
        Assert.assertEquals("7",TestControllerUtils.invoke(reg, "plus", params) );
    }

    @Test
    public  void canOverrideMethodOnImplementedContract() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "6";
        Assert.assertEquals("-9",TestControllerUtils.invoke(reg, "minus", params) );
    }

}


@Controller(name = "plusController")
 interface AdditionControllerContract{
    @ControllerAction(path = "plus")
    default Integer plus(Integer addition){
        return getCurrent()+addition;
    }
    Integer getCurrent();
}

@Controller(name = "minusController")
 interface MinusControllerContract{
    @ControllerAction(path = "minus")
    default Integer minus(Integer integer){
        return getCurrent()-integer;
    }
    Integer getCurrent();
}

@Controller(name = "composedController")
class ComposedController implements AdditionControllerContract, MinusControllerContract{
    @Override
    @ControllerAction(path = "getCurrent")
    public Integer getCurrent() {
        return 1;
    }
    @ControllerAction(path = "minus")
    public Integer minus(Integer addition){
        return getCurrent()-10;
    }
}
