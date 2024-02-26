package tcpassos.pipeline;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A collection of single-step pipelines.
 */
public class Pipes {

    private Pipes() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a single-step pipeline that will always return the value given as an output element
     *
     * @param <T> Type of input and output element
     * @param value Value to be returned by the pipeline
     * @return {@code Pipeline<T, T>}
     */
    public static <T> Pipeline<Void, T> giving(T value) {
        return (obj) -> Optional.of(value);
    }

    /**
     * Returns a single-step pipeline that will always return the value given by the supplier as an output element
     *
     * @param <T> Type of input and output element
     * @param supplier Supplier that will provide the output element
     * @return {@code Pipeline<T, T>}
     */
    public static <T> Pipeline<Void, T> giving(Supplier<T> supplier) {
        return (obj) -> Optional.of(supplier.get());
    }

    /**
     * Returns a single-step pipeline that will process the input element and return the same element
     * to the next stage
     *
     * @param <T> Type of input and output element
     * @param processor Consumer that will process the input element
     * @return {@code Pipeline<T, T>}
     */
    public static <T> Pipeline<T, T> processing(Consumer<T> processor) {
        return (obj) -> {
            processor.accept(obj);
            return Optional.of(obj);
        };
    }

    /**
     * Returns a single-step pipeline that will run a code and return the same input element to the next stage
     *
     * @param <T> Input and output element type
     * @param runnable Runnable to be executed
     * @return {@code Pipeline<T, T>}
     */
    public static <T> Pipeline<T, T> running(Runnable runnable) {
        return (obj) -> {
            runnable.run();
            return Optional.of(obj);
        };
    }

    /**
     * Returns a single-step pipeline that will process the input element and return the same element
     * to the next stage if the element is valid according to the filter, null otherwise
     *
     * @param <T> Type of input and output element
     * @param filter Predicate that will filter the element processed by the pipeline
     * @return {@code Pipeline<T, T>}
     */
    public static <T> Pipeline<T, T> filtering(Predicate<T> filter) {
        return (obj) -> filter.test(obj) ? Optional.of(obj) : Optional.empty();
    }

    /**
     * Returns a pipeline that applies the given filter and mapping function to the input object.
     * If the filter test passes, the mapping function is applied and the result is wrapped in an Optional.
     * If the filter test fails, an empty Optional is returned.
     *
     * @param filter the predicate used to filter the input object
     * @param ifTrue the function used to map the filtered input object to the desired result
     * @param <T> the type of the input object
     * @param <R> the type of the result
     * @return a pipeline that applies the filter and mapping function to the input object
     */
    public static <T, R> Pipeline<T, R> filterMapping(Predicate<T> filter, Function<? super T, R> ifTrue) {
        return (obj) -> filter.test(obj) ? Optional.ofNullable(ifTrue.apply(obj)) : Optional.empty();
    }

    /**
     * Returns a pipeline that applies a filter to the input object and maps it to a result based on the filter's evaluation.
     *
     * @param filter  the predicate used to filter the input object
     * @param ifTrue  the function to apply to the input object if the filter evaluates to true
     * @param ifFalse the function to apply to the input object if the filter evaluates to false
     * @param <T>     the type of the input object
     * @param <R>     the type of the result
     * @return a pipeline that applies the filter and mapping functions to the input object
     */
    public static <T, R> Pipeline<T, R> filterMapping(Predicate<T> filter, Function<? super T, R> ifTrue, Function<? super T, R> ifFalse) {
        return (obj) -> filter.test(obj) ? Optional.ofNullable(ifTrue.apply(obj)) : Optional.ofNullable(ifFalse.apply(obj));
    }

    /**
     * Returns a single-step pipeline that will process the input element from a function and
     * return the transformed element
     *
     * @param <T> Input element type
     * @param <R> Output element type
     * @param mapper Function that will transform the input element
     * @return {@code Pipeline<T, R>}
     */
    public static <T, R> Pipeline<T, R> mapping(Function<? super T, R> mapper) {
        return (obj) -> Optional.of(mapper.apply(obj));
    }
    
}
