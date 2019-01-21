package com.github.davidmoten.grumpy.function;

@FunctionalInterface
public interface Function<T, R> {

    R apply(T value);
    
}
