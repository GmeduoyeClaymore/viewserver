package io.viewserver.util.dynamic;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface ClassInterpreter<T> {

    static <T> ClassInterpreter<T> cached(ClassInterpreter<T> interpreter) {
        return Memoizer.memoize(interpreter::interpret)::apply;
    }

    static <T> ClassInterpreter<T> mappingWith(UnboundMethodInterpreter<T> interpreter) {
        return iface -> {
            TypeInfo typeInfo = TypeInfo.forType(iface);
            Map<Method, UnboundMethodCallHandler<T>> collect = getAllMethods(typeInfo.getRawType()).stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            interpreter::interpret
                    ));
            return key -> {
                System.out.println(iface + "" + typeInfo);
                return collect.get(key);
            };
        };
    }

    public static Set<Method> getAllMethods(Class<?> cl) {
        Set<Method> methods=new LinkedHashSet<>();
        for(Method meth : cl.getMethods()){
            methods.add(meth);
        }
        Map<Object,Set<Package>> types=new HashMap<>();
        final Set<Package> pkgIndependent = Collections.emptySet();
        for(Method m: methods) types.put(methodKey(m), pkgIndependent);
        for(Class<?> current=cl; current!=null; current=current.getSuperclass()) {
            for(Method m: current.getDeclaredMethods()) {
                final int mod = m.getModifiers(),
                        access=Modifier.PUBLIC;
                if(!Modifier.isStatic(mod)) switch(mod&access) {
                    case Modifier.PUBLIC: continue;
                    default:
                        Set<Package> pkg=
                                types.computeIfAbsent(methodKey(m), key -> new HashSet<>());
                        if(pkg!=pkgIndependent && pkg.add(current.getPackage())) break;
                        else continue;
                    case Modifier.PROTECTED:
                        if(types.putIfAbsent(methodKey(m), pkgIndependent)!=null) continue;
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        continue;
                }
                if(!Modifier.isPrivate(m.getModifiers())){
                    methods.add(m);
                }
            }
        }
        return methods;
    }


    static Object methodKey(Method m) {
        return Arrays.asList(m.getName(),
                MethodType.methodType(m.getReturnType(), m.getParameterTypes()));
    }

    UnboundMethodInterpreter<T> interpret(Class<?> iface);

}
