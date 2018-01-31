import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerRegistration;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;

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
