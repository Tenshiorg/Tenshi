package io.github.shadow578.tenshi.lang;

/**
 * a simple function with two parameters and a return value
 * @param <Rt> the return type
 * @param <P1> the type of the first parameter
 * @param <P2> the type of the second parameter
 */
public interface BiFunction<Rt, P1, P2> {
    /**
     * invoke the function
     * @param param1 the first parameter
     * @param param2 the second parameter
     * @return the return value
     */
    Rt invoke(P1 param1, P2 param2);
}
