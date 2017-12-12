package io.viewserver.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ControllerRegistration{

    private final Controller controller;
    private final HashMap<String,ControllerActionEntry> actions;

    public ControllerRegistration(Object controller){
        if(controller == null){
            throw new RuntimeException("Controller must be specified");
        }
       this.controller = getControllerAnnotation(controller);
       this.actions = getActions(controller);
    }

    public String getName(){
        return this.controller.name();
    }

    public Map<String,ControllerActionEntry> getActions(){
        return this.actions;
    }

    private HashMap<String, ControllerActionEntry> getActions(Object controller) {
        Method[] methods = controller.getClass().getDeclaredMethods();
        final HashMap<String,ControllerActionEntry> result = new HashMap<String,ControllerActionEntry>();

        for(Method m : methods)
        {
            for(Annotation a : m.getAnnotations())
            {
                if ( a instanceof ControllerAction)
                {
                    ControllerAction an = (ControllerAction)a;
                    result.put(an.path(),new ControllerActionEntry(m,controller,an,this.controller));
                }
            }
        }
        return result;
    }


    Controller getControllerAnnotation(Object controller){
        for(Annotation a : controller.getClass().getAnnotations()){
            if(a instanceof Controller){
                return (Controller)a;
            }
        }
        throw new RuntimeException("Invalid controller registration. Cannot find Controller parameter on type " + controller);
    }
}
