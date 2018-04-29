import io.viewserver.controller.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

public class FutureControllerTest {

    private FutureController sut;


    @Before
    public void createSut(){
        sut = new FutureController();
    }

    @Test
    public  void canInvokeFutureWithParam() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        String params = "6";
        System.out.println(TestControllerUtils.invoke(reg, "plus", params));
    }

    @Test
    public  void canInvokeFutureWithoutParam() throws NoSuchMethodException {
        ControllerRegistration reg = new ControllerRegistration(sut);
        System.out.println(TestControllerUtils.invoke(reg, "plusOne", null));
    }
}


