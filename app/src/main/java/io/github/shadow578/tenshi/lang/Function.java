package io.github.shadow578.tenshi.lang;

/**
 * a simple function with one parameter and return value
 * @param <Rt> the return type
 * @param <P1> the type of the first paramter
 */
public interface Function<Rt, P1> {
    /**
     * invoke the function
     * @param p the first paramter
     * @return the return value
     */
    Rt invoke(P1 p);
}
