import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;

import java.util.concurrent.Executors;

@Controller(name = "futureController")
public class FutureController{

    private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
    Integer current = 0;

    @ControllerAction(path = "plus")
    public ListenableFuture<Integer> plus(Integer addition){
        return service.submit(() -> current+=addition);
    }

    @ControllerAction(path = "plusOne")
    public ListenableFuture<Integer> plusone(){
        return service.submit(() -> current+=1);
    }

}
