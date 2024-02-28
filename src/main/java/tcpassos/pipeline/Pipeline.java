package tcpassos.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import tcpassos.pipeline.classes.BranchedPipelineImpl;
import tcpassos.pipeline.classes.PipelineBuilderImpl;

/**
 * Processing pipeline using generics.
 *
 * @param <BEGIN> Pipeline input element type
 * @param <END> Pipeline output element type
 *
 * <p>
 * The method of this functional interface will return a {@link Optional} with the output element or a
 * {@code Optional.empty()} if the pipeline is interrupted before reaching the end.
 * </p>
 */
public interface Pipeline <BEGIN, END> extends BasePipeline <BEGIN, Optional<END>> {

    /**
     * Returns a builder to create a pipeline from scratch
     *
     * @param <T> Input and output element type
     * @return {@code Builder<T, T>}
     */
    static <T> Builder<T, T> builder() {
        return new PipelineBuilderImpl<>(empty());
    }

    /**
     * Returns a builder to create a pipeline from an existing pipeline
     *
     * @param <T> Input element type of the pipeline
     * @param <R> Output element type of the pipeline
     * @param pipeline Existing pipeline
     * @return {@code Builder<T, T>}
     */
    static <T, R> Builder<T, R> builder(Pipeline<T, R> pipeline) {
        return new PipelineBuilderImpl<>(pipeline);
    }

    /**
     * Returns an empty pipeline whose output will be the same input element
     *
     * @param <T> Type of input and output element
     * @return {@code Pipeline<T, T>}
     */
    static <T> Pipeline<T, T> empty() {
        return (obj) -> Optional.ofNullable(obj);
    }

    /**
     * Returns an asynchronous pipeline that wraps this pipeline.
     *
     * @return the asynchronous pipeline
     */
    default AsyncPipeline<BEGIN, END> async() {
        return AsyncPipeline.of(this);
    }

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
     * Connect this pipeline at the beginning of another pipeline
     *
     * @param <NEW_END> New pipeline output element type
     * @param nextPipe Pipeline to be connected at the end of this pipeline
     * @return {@code Pipeline<BEGIN, NEW_END>}
     */
    default <NEW_END> Pipeline<BEGIN, NEW_END> connect(BasePipeline<? super END, Optional<NEW_END>> nextPipe) {
        return (BEGIN obj) -> {
            Optional<END> newObjOpt = execute(obj);
            if (newObjOpt.isPresent()) {
                return nextPipe.execute(newObjOpt.get());
            }
            return Optional.empty();
        };
    }

    /**
     * Connects the current pipeline to a collection of next pipelines.
     * Returns a branched pipeline that represents the connection between the current pipeline and the next pipelines.
     *
     * @param next the collection of next pipelines to connect to
     * @param <NEW_END> the type of the new end of the branched pipeline
     * @return a branched pipeline representing the connection between the current pipeline and the next pipelines
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> connect(Collection<Pipeline<END, NEW_END>> next) {
        return new BranchedPipelineImpl<BEGIN, END, NEW_END>(this, next);
    }

    /**
     * Connects the current pipeline to a void pipeline, ignoring the result of the current pipeline.
     * 
     * @param nextPipe the void pipeline to connect to
     * @param <NEW_END> the type of the new end result
     * @return a branched pipeline that executes the current pipeline and then the void pipeline
     */
    default <NEW_END> Pipeline<BEGIN, NEW_END> connectVoid(BasePipeline<Void, Optional<NEW_END>> nextPipe) {
        return (BEGIN obj) -> {
            execute(obj);
            return nextPipe.execute();
        };
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
     * Builder with methods to add stages to the pipeline
     *
     * @param <BEGIN> Input element type of the pipeline
     * @param <END> Output element type of the pipeline
     */
    public interface Builder <BEGIN, END> extends BasePipeline.Builder<BEGIN, END> {

        /**
         * Forks the pipeline into a new branch, allowing for parallel processing of the pipeline.
         *
         * @param <NEW_END> the type of the end result of the new branch
         * @param forkBuilderFunction the function that defines the branching logic for the new branch
         * @return a new builder representing the forked branch of the pipeline
         */
        <NEW_END> BranchedPipeline.Builder <BEGIN, NEW_END> fork(Function<Builder<BEGIN, END>, Builder<BEGIN, NEW_END>> forkBuilderFunction);

    }

}