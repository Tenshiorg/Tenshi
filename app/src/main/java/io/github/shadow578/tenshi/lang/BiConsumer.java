package io.github.shadow578.tenshi.lang;

/**
 * a simple function with two parameters and no return value
 * @param <P1> the type of the first parameter
 * @param <P2> the type of the second parameter
 */
public interface BiConsumer<P1,P2> {
    /**
     * invoke the function
     * @param param1 the first parameter
     * @param param2 the second parameter
     */
    void invoke(P1 param1, P2 param2);
}
