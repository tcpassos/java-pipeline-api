package tcpassos.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A pipeline that receives an input element and returns an output element of the same type.
 *
 * @param <T> Input and output element type
 */
public interface UnaryPipeline <T> extends OptionalPipeline <T,T> {

    /**
     * Returns a builder to create a pipeline from scratch
     *
     * @param <T> Input and output element type
     * @return {@code Builder<T>}
     */
    static <T> Builder<T> builder() {
        return new Pipelines.UnaryPipelineBuilderImpl<>(empty());
    }

    /**
     * Returns a builder to create a pipeline from an existing pipeline
     *
     * @param <T> Input and output element type
     * @param pipeline Existing pipeline
     * @return {@code Builder<T>}
     */
    static <T> Builder<T> builder(UnaryPipeline<T> pipeline) {
        return new Pipelines.UnaryPipelineBuilderImpl<>(pipeline);
    }

    /**
     * Returns an empty pipeline whose output will be the same input element
     *
     * @param <T> Type of input and output element
     * @return {@code UnaryPipeline<T>}
     */
    static <T> UnaryPipeline<T> empty() {
        return (obj) -> Optional.ofNullable(obj);
    }

    /**
     * Connect this pipeline at the beginning of another pipeline
     *
     * @param nextPipe Pipeline to be connected at the end of this pipeline
     * @return {@code UnaryPipeline<T>}
     */
    default UnaryPipeline<T> connect(BasePipeline<? super T, Optional<T>> nextPipe) {
        return (T obj) -> {
            Optional<T> newObjOpt = execute(obj);
            if (newObjOpt.isPresent()) {
                return nextPipe.execute(newObjOpt.get());
            }
            return Optional.empty();
        };
    }

    /**
     * Connect this pipeline with multiple pipelines generating a branched pipeline
     *
     * @param <NEW_END> New pipeline output element type
     * @param nextPipes Pipelines to be connected at the end of this pipeline
     * @return {@code BranchedPipeline<BEGIN, NEW_END>}
     */
    default <NEW_END> BranchedPipeline<T, NEW_END> connect(Collection<BasePipeline<? super T, Optional<NEW_END>>> nextPipes) {
        return (input) -> {
            List<NEW_END> results = new ArrayList<>();
            execute(input).ifPresent(output -> {
                for (var nextPipe : nextPipes) {
                    nextPipe.execute(output).ifPresent(results::add);
                }
            });
            return results;
        };
    }

    /**
     * Connects the current pipeline to the specified branched pipeline.
     * 
     * @param nextPipe the branched pipeline to connect to
     * @param <NEW_END> the type of the end result of the branched pipeline
     * @return the connected branched pipeline
     */
    default <NEW_END> BranchedPipeline<T, NEW_END> connect(BranchedPipeline<? super T, NEW_END> nextPipe) {
        return (input) -> execute(input)
            .map(nextPipe::execute)
            .orElse(Collections.emptyList());
    }

    /**
     * Connect this pipeline at the beginning of another pipeline without checking if the output element is present
     *
     * @param nextPipe Pipeline to be connected at the end of this pipeline
     * @return {@code UnaryPipeline<T>}
     */
    default UnaryPipeline<T> forceConnect(BasePipeline<? super T, Optional<T>> nextPipe) {
        return (T obj) -> {
            Optional<T> newObjOpt = execute(obj);
            return newObjOpt.isPresent() ? nextPipe.execute(newObjOpt.get()) : nextPipe.execute(null);
        };
    }

    /**
     * Builder with methods to add stages to the pipeline
     *
     * @param <T> Input and output element type of the pipeline
     */
    public interface Builder <T> {

        /**
         * Adds a stage to give an input element to the pipeline
         *
         * @param obj Input element
         * @return {@code Builder<T>}
         */
        Builder <T> give(T obj);

        /**
         * Adds a stage to give an input element to the pipeline
         *
         * @param supplier Supplier of the input element
         * @return {@code Builder<T>}
         */
        Builder <T> give(Supplier<T> supplier);

        /**
        * Adds a stage to filter the output element of the pipeline
        *
        * @param filter Filter
        * @return {@code Builder<T>}
        */
        Builder <T> filter(Predicate<T> filter);

        /**
         * Adds a stage to transform the output element of the pipeline
         *
         * @param mapper Function to transform the output element
         * @return {@code Builder<T>}
         */
        Builder <T> map(UnaryOperator<T> mapper);

        /**
         * Adds a stage to pipe the output element of the pipeline to another pipeline
         *
         * @param nextPipe Pipeline to be connected at the end of this pipeline
         * @return {@code Builder<T>}
         */
        Builder <T> pipe(OptionalPipeline<? super T, T> nextPipe);

        /**
         * Adds a stage to process the output element of the pipeline
         *
         * @param processor Element consumer
         * @return {@code Builder<T>}
         */
        Builder <T> process(Consumer<T> processor);

        /**
         * Adds a stage to run a code
         *
         * @param runnable Runnable to be executed
         * @return {@code Builder<T>}
         */
        Builder <T> run(Runnable runnable);

        /**
         * Builds the pipeline
         *
         * @return {@code Pipeline<T>}
         */
        UnaryPipeline<T> build();

    }

}
