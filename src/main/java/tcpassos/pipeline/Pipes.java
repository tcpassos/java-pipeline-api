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

    /**
     * Returns a single-step pipeline that will always return the value given as an output element
     *
     * @param <T> Type of input and output element
     * @param value Value to be returned by the pipeline
     * @return {@code Pipeline<T, T>}
     */
    static <T> Pipeline<T, T> giving(T value) {
        return (obj) -> Optional.of(value);
    }

    /**
     * Returns a single-step pipeline that will always return the value given by the supplier as an output element
     *
     * @param <T> Type of input and output element
     * @param supplier Supplier that will provide the output element
     * @return {@code Pipeline<T, T>}
     */
    static <T> Pipeline<T, T> giving(Supplier<T> supplier) {
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
    static <T> Pipeline<T, T> processing(Consumer<T> processor) {
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
    static <T> Pipeline<T, T> running(Runnable runnable) {
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
    static <T> Pipeline<T, T> filtering(Predicate<T> filter) {
        return (obj) -> filter.test(obj) ? Optional.of(obj) : Optional.empty();
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
    static <T, R> Pipeline<T, R> mapping(Function<? super T, R> mapper) {
        return (obj) -> Optional.of(mapper.apply(obj));
    }
    
}
