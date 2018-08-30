package io.viewserver.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FutureControllerTest {

    private FutureController sut;


    @Before
    public void createSut(){
        FutureLoggerInterceptor.reset();
        sut = new FutureController();
    }

    @Test
    public  void canInvokeFutureWithParam() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "6";
        System.out.println(TestControllerUtils.invoke(reg, "plus", params));
    }

    @Test
    public  void canInvokeFutureWithParamAndInterceptor() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "6";
        System.out.println(TestControllerUtils.invoke(reg, "plusIntercepted", params));
        Assert.assertEquals(new Integer(6),FutureLoggerInterceptor.plusInterceptedAddition);
    }


    @Test
    public  void canInvokeFutureWithoutParam() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        System.out.println(TestControllerUtils.invoke(reg, "plusOne", null));
    }

    @Test
    public  void canInvokeFutureWithoutParamAndInterceptor() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        System.out.println(TestControllerUtils.invoke(reg, "plusOneIntercepted", null));
        Assert.assertTrue(FutureLoggerInterceptor.plusOneIntercepted);
    }
}


