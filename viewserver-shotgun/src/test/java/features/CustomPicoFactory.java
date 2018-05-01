package features;


import com.shotgun.viewserver.ShotgunServerLauncher;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Utils;
import cucumber.runtime.java.picocontainer.PicoFactory;
import io.viewserver.server.steps.ViewServerClientContext;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CustomPicoFactory implements ObjectFactory {
    private final ViewServerClientContext context;
    private final ShotgunServerLauncher launcher;
    private MutablePicoContainer pico;
    private final Set<Class<?>> classes = new HashSet();

    public CustomPicoFactory() {
        context = new ViewServerClientContext();
        launcher = new ShotgunServerLauncher();
    }

    public void start() {
        this.pico = (new PicoBuilder()).withCaching().withLifecycle().build();
        this.pico.addComponent(context);
        this.pico.addComponent(launcher);
        Iterator var1 = this.classes.iterator();

        while(var1.hasNext()) {
            Class<?> clazz = (Class)var1.next();
            try {
                this.pico.addComponent(clazz);
            }catch (Exception ex){
                System.out.println("Not re-registering" + clazz);
            }
        }

        this.pico.start();
    }

    public void stop() {
        this.pico.stop();
        this.pico.dispose();
    }

    public boolean addClass(Class<?> clazz) {
        if (Utils.isInstantiable(clazz) && this.classes.add(clazz)) {
            this.addConstructorDependencies(clazz);
        }

        return true;
    }

    public <T> T getInstance(Class<T> type) {
        return this.pico.getComponent(type);
    }

    private void addConstructorDependencies(Class<?> clazz) {
        Constructor[] var2 = clazz.getConstructors();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Constructor constructor = var2[var4];
            Class[] var6 = constructor.getParameterTypes();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                Class paramClazz = var6[var8];
                this.addClass(paramClazz);
            }
        }

    }
}

