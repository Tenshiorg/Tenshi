package io.github.shadow578.tenshi.lang;

/**
 * a simple function with no parameters and a return value
 * @param <Rt> the return type
 */
public interface Action<Rt> {
    /**
     * invoke the function
     * @return the returned value
     */
    Rt invoke();
}
