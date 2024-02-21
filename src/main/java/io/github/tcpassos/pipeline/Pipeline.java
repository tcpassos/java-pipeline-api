package io.github.tcpassos.pipeline;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
public interface Pipeline <BEGIN, END> extends OptionalPipeline <BEGIN,END> {


    /**
     * Returns a builder to create a pipeline from scratch
     *
     * @param <T> Input and output element type
     * @return {@code Builder<T, T>}
     */
    static <T> Builder<T, T> builder() {
        return new Pipelines.PipelineBuilderImpl<>(empty());
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
        return new Pipelines.PipelineBuilderImpl<>(pipeline);
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
     * Connect this pipeline at the beginning of another pipeline
     *
     * @param <NEW_END> New pipeline output element type
     * @param nextPipe Pipeline to be connected at the end of this pipeline
     * @return {@code Pipeline<BEGIN, NEW_END>}
     */
    default <NEW_END> Pipeline<BEGIN, NEW_END> connect(OptionalPipeline<? super END, NEW_END> nextPipe) {
        return (BEGIN obj) -> {
            Optional<END> newObjOpt = execute(obj);
            if (newObjOpt.isPresent()) {
                return nextPipe.execute(newObjOpt.get());
            }
            return Optional.empty();
        };
    }

    /**
     * Connect this pipeline at the beginning of another pipeline without checking if the output element is present
     *
     * @param <NEW_END> New pipeline output element type
     * @param nextPipe Pipeline to be connected at the end of this pipeline
     * @return {@code UnaryPipeline<T>}
     */
    default <NEW_END> Pipeline<BEGIN, NEW_END> forceConnect(OptionalPipeline<? super END, NEW_END> nextPipe) {
        return (BEGIN obj) -> {
            Optional<END> newObjOpt = execute(obj);
            return newObjOpt.isPresent() ? nextPipe.execute(newObjOpt.get()) : nextPipe.execute(null);
        };
    }

    /**
     * Builder with methods to add stages to the pipeline
     *
     * @param <BEGIN> Input element type of the pipeline
     * @param <END> Output element type of the pipeline
     */
    public interface Builder <BEGIN, END> {

        /**
         * Adds a stage to give an input element to the pipeline
         *
         * @param value Output element
         * @return {@code Builder<BEGIN, END>}
         */
        Builder <BEGIN, END> give(END value);

        /**
         * Adds a stage to give an input element to the pipeline
         *
         * @param supplier Supplier of the output element
         * @return {@code Builder<BEGIN, END>}
         */
        Builder <BEGIN, END> give(Supplier<END> supplier);

        /**
        * Adds a stage to filter the output element of the pipeline
        *
        * @param filter Filter
        * @return {@code Builder<BEGIN, END>}
        */
        Builder <BEGIN, END> filter(Predicate<END> filter);

        /**
         * Adds a stage to transform the output element of the pipeline
         *
         * @param <NEW_END> New output element type
         * @param mapper Function to transform the output element
         * @return {@code Builder<BEGIN, NEW_END>}
         */
        <NEW_END> Builder <BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper);

        /**
         * Adds a stage to pipe the output element of the pipeline to another pipeline
         *
         * @param <NEW_END> New output element type
         * @param nextPipe Pipeline to be connected at the end of this pipeline
         * @return {@code Builder<BEGIN, NEW_END>}
         */
        <NEW_END> Builder <BEGIN, NEW_END> pipe(OptionalPipeline<? super END, NEW_END> nextPipe);

        /**
         * Adds a stage to process the output element of the pipeline
         *
         * @param processor Element consumer
         * @return {@code Builder<BEGIN, END>}
         */
        Builder <BEGIN, END> process(Consumer<END> processor);

        /**
         * Adds a stage to run a code
         *
         * @param runnable Runnable to be executed
         * @return {@code Builder<BEGIN, END>}
         */
        Builder <BEGIN, END> run(Runnable runnable);

        /**
         * Builds the pipeline
         *
         * @return {@code Pipeline<BEGIN, END>}
         */
        Pipeline<BEGIN, END> build();

    }

}