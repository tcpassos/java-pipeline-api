package com.github.tcpassos.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Processing pipeline using generics that returns an {@link Optional} with the output element or a
 * {@code Optional.empty()} if the pipeline is interrupted before reaching the end.
 *
 * @param <BEGIN> Pipeline input element type
 * @param <END> Pipeline output element type
 */
public interface OptionalPipeline <BEGIN, END> extends BasePipeline <BEGIN, Optional<END>> {

    /**
     * Returns a supplier representation of the pipeline
     *
     * @return {@code Supplier<Optional<END>>}
     */
    default Supplier<Optional<END>> asSupplier() {
        return this::execute;
    }

    /**
     * Returns a supplier representation of the pipeline that returns null if the output element is empty
     *
     * @return {@code Supplier<END>}
     */
    default Supplier<END> asNullableSupplier() {
        return () -> execute().orElse(null);
    }

    /**
     * Returns a consumer representation of the pipeline
     *
     * @return {@code Consumer<BEGIN>}
     */
    default Consumer<BEGIN> asConsumer() {
        return this::execute;
    }

    /**
     * Returns a runnable representation of the pipeline
     *
     * @return {@code Runnable}
     */
    default Runnable asRunnable() {
        return () -> execute();
    }

    /**
     * Returns a predicate representation of the pipeline
     *
     * @return {@code Predicate<BEGIN>}
     */
    default Predicate<BEGIN> asPredicate() {
        return (obj) -> execute(obj).isPresent();
    }

    /**
     * Returns a function representation of the pipeline
     *
     * @return {@code Function<BEGIN, Optional<END>>}
     */
    default Function<BEGIN, Optional<END>> asFunction() {
        return this::execute;
    }

    /**
     * Returns a function representation of the pipeline that returns null if the output element is empty
     *
     * @return {@code Function<BEGIN, END>}
     */
    default Function<BEGIN, END> asNullableFunction() {
        return (obj) -> execute(obj).orElse(null);
    }

    /**
     * Executes the pipeline with multiple input elements
     *
     * @param elements Input elements
     * @return {@code List<END>}
     */
    default List<END> executeBatch(Collection<BEGIN> elements) {
        return elements.stream()
                       .map(this::execute)
                       .flatMap(Optional::stream)
                       .collect(Collectors.toList());
    }

    /**
     * Executes the pipeline asynchronously
     *
     * @param obj Input element
     * @return {@code CompletableFuture<END>}
     */
    default CompletableFuture<END> executeAsync(BEGIN obj) {
        return CompletableFuture.supplyAsync(() -> execute(obj).orElse(null));
    }
    
    /**
     * Executes the pipeline asynchronously without an input element
     *
     * @return {@code CompletableFuture<END>}
     */
    default CompletableFuture<END> executeAsync() {
        return CompletableFuture.supplyAsync(() -> execute().orElse(null));
    }

    /**
     * Creates a stream with the output elements that are processed by this pipeline
     *
     * @param elements Input elements
     * @return {@code Stream<END>}
     */
    default Stream<END> streamOf(BEGIN[] elements) {
        return Stream.of(elements)
                     .map(element -> execute(element))
                     .flatMap(Optional::stream);
    }

    /**
     * Creates a stream with the output elements that are processed by this pipeline
     *
     * @param elements Iterable of input elements
     * @return {@code Stream<END>}
     */
    default Stream<END> streamOf(Iterable<BEGIN> elements) {
        return StreamSupport.stream(elements.spliterator(), false)
                            .map(element -> execute(element))
                            .flatMap(Optional::stream);
    }

    /**
     * Creates a stream with the output elements that are processed by this pipeline
     *
     * @param elements Stream of input elements
     * @return {@code Stream<END>}
     */
    default Stream<END> streamOf(Stream<BEGIN> elements) {
        return elements.map(element -> execute(element))
                       .flatMap(Optional::stream);
    }
    
}
