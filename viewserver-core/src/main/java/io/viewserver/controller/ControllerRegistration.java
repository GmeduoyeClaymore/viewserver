package io.viewserver.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.viewserver.util.dynamic.ClassInterpreter.getAllMethods;

public class ControllerRegistration{

    private final Controller controller;
    private final HashMap<String,ControllerActionEntry> actions;

    public ControllerRegistration(Object controller){
        if(controller == null){
            throw new RuntimeException("Controller must be specified");
        }
       this.controller = getControllerAnnotation(controller);
        try{
            this.actions = getActions(controller);
        }catch (Exception ex){
            throw new RuntimeException(String.format("Problem constructing controller \"%s\"",this.controller.name()));
        }
    }

    public String getName(){
        return this.controller.name();
    }

    public Map<String,ControllerActionEntry> getActions(){
        return this.actions;
    }

    private HashMap<String, ControllerActionEntry> getActions(Object controller) {
        Set<Method> methods = getAllMethods(controller.getClass());
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
