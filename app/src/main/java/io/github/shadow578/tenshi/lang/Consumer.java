package io.github.shadow578.tenshi.lang;

/**
 * a simple function with one parameter and no return value
 * @param <P1> the type of the parameter
 */
public interface Consumer<P1> {
    /**
     * invoke the function
     * @param p the first parameter
     */
    void invoke(P1 p);
}
