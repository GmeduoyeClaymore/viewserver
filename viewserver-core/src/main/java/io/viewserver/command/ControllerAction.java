package io.viewserver.command;

/**
 * Created by Gbemiga on 12/12/17.
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerAction {
    String path();
    boolean isSynchronous() default true;
}