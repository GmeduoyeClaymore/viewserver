package io.viewserver.util.dynamic;

public interface Function2<T,U,R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t, U u);
}
