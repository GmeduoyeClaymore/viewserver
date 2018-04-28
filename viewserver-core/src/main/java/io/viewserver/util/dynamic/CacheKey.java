package io.viewserver.util.dynamic;

import java.util.Objects;

public class CacheKey<I, B> {
    private Object i;
    private Object b;

    public <I, B> CacheKey(I i, B b) {
        this.i = i;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey<?, ?> cacheKey = (CacheKey<?, ?>) o;
        return Objects.equals(i, cacheKey.i) &&
                Objects.equals(b, cacheKey.b);
    }

    @Override
    public int hashCode() {

        return Objects.hash(i, b);
    }
}
