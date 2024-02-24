package tcpassos.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a branched pipeline that takes an input of type BEGIN and produces a list of outputs of type END.
 *
 * @param <BEGIN> The type of the input element
 * @param <END> The type of the output element
 */
public interface BranchedPipeline <BEGIN, END> extends BasePipeline<BEGIN, List<END>> {

    /**
     * Returns a builder to create a branched pipeline from scratch.
     *
     * @param <T> the type of the input and output elements
     * @return a new builder for a branched pipeline
     */
    static <T> Builder<T, T> builder() {
        return new Pipelines.BranchedPipelineBuilderImpl<>(empty());
    }

    /**
     * Creates a new builder for constructing a branched pipeline.
     *
     * @param pipeline the optional pipeline to be executed
     * @param <BEGIN>  the type of the input to the pipeline
     * @param <END>    the type of the output from the pipeline
     * @return a new builder for constructing a branched pipeline
     */
    static <BEGIN, END> Builder<BEGIN, END> builder(OptionalPipeline<BEGIN, END> pipeline) {
        BranchedPipeline<BEGIN, END> branchedPipeline = (input) -> pipeline.execute(input).stream().toList();
        return new Pipelines.BranchedPipelineBuilderImpl<>(branchedPipeline);
    }

    /**
     * Creates a new builder for the given BranchedPipeline.
     *
     * @param pipeline the BranchedPipeline instance to build
     * @param <BEGIN>  the type of the input to the pipeline
     * @param <END>    the type of the output from the pipeline
     * @return a new Builder instance for the given BranchedPipeline
     */
    static <BEGIN, END> Builder<BEGIN, END> builder(BranchedPipeline<BEGIN, END> pipeline) {
        return new Pipelines.BranchedPipelineBuilderImpl<>(pipeline);
    }

    /**
     * Returns an empty branched pipeline.
     *
     * @param <T> the type of the input and output elements
     * @return an empty branched pipeline
     */
    static <T> BranchedPipeline<T, T> empty() {
        return (input) -> List.of();
    }

    /**
     * Creates a branched pipeline by combining the current pipeline with another pipeline.
     * The resulting pipeline will execute both pipelines in parallel and return the combined results.
     *
     * @param branch The pipeline to be branched and executed in parallel.
     * @return A new branched pipeline that combines the results of the current pipeline and the branch pipeline.
     */
    default BranchedPipeline <BEGIN, END> branch(Pipeline<BEGIN, END> branch) {
        return (input) -> {
            List<END> results = execute();
            branch.execute(input).ifPresent(results::add);
            return results;
        };
    }

    /**
     * Connects the current pipeline to the given nextPipe, creating a branched pipeline.
     * The nextPipe is executed for each element in the output of the current pipeline,
     * and the resulting elements are collected into a new list.
     *
     * @param nextPipe the next pipeline to connect to
     * @param <NEW_END> the type of the elements produced by the next pipeline
     * @return a new branched pipeline
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> connect(BasePipeline<? super END, Optional<NEW_END>> nextPipe) {
        return (input) -> execute(input).stream()
                                        .map(nextPipe::execute)
                                        .flatMap(Optional::stream)
                                        .toList();
    }

    /**
     * Connects the current pipeline to the specified nextPipe, creating a new branched pipeline.
     * The input of the current pipeline is passed to the execute method of the current pipeline,
     * and the resulting output is passed as input to the execute method of the nextPipe.
     * The outputs of both pipelines are combined into a single list and returned as the result.
     *
     * @param nextPipe the branched pipeline to connect to
     * @param <NEW_END> the type of the output of the nextPipe
     * @return a new branched pipeline that combines the outputs of the current pipeline and the nextPipe
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> connect(BranchedPipeline<? super END, NEW_END> nextPipe) {
        return (input) -> execute(input)
            .stream()
            .map(nextPipe::execute)
            .reduce(new ArrayList<>(), (acc, list) -> {
                acc.addAll(list);
                return acc;
            });
    }

    /**
     * Connects the current pipeline to a void pipeline, creating a new branched pipeline.
     * The new pipeline takes the input from the current pipeline, executes it, and then passes the results to the void pipeline.
     * The void pipeline is executed for each result, and the non-empty results are collected into a list.
     *
     * @param nextPipe the void pipeline to connect to
     * @param <NEW_END> the type of the new pipeline's end result
     * @return a new branched pipeline that connects the current pipeline to the void pipeline
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> connectVoid(BasePipeline<Void, Optional<NEW_END>> nextPipe) {
        return (input) -> {
            List<END> results = execute(input);
            return Stream.generate(() -> nextPipe.execute())
                        .limit(results.size())
                        .flatMap(Optional::stream)
                        .toList();
        };
    }

    /**
     * Connects this branched pipeline to another branched pipeline that takes no input.
     * Executes this pipeline with the given input, and then executes the next pipeline for each result.
     * Returns a new branched pipeline that collects the results of executing the next pipeline.
     *
     * @param nextPipe the next branched pipeline to connect to
     * @param <NEW_END> the type of the output of the next pipeline
     * @return a new branched pipeline that collects the results of executing the next pipeline
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> connectVoid(BranchedPipeline<Void, NEW_END> nextPipe) {
        return (input) -> {
            List<END> results = execute(input);
            return Stream.generate(() -> nextPipe.execute())
                        .limit(results.size())
                        .reduce(new ArrayList<>(), (acc, list) -> {
                            acc.addAll(list);
                            return acc;
                        });
        };
    }

    /**
     * Connects the current pipeline to the specified next pipeline, forcing the connection even if the current pipeline has no results.
     *
     * @param nextPipe the next pipeline to connect to
     * @param <NEW_END> the type of the new end result of the branched pipeline
     * @return a new branched pipeline that connects the current pipeline to the specified next pipeline
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> forceConnect(BasePipeline<? super END, Optional<NEW_END>> nextPipe) {
        return (input) -> {
            List<END> results = execute(input);
            if (results.isEmpty()) {
                return nextPipe.execute(null).stream().toList();
            }
            return results.stream()
                          .map(nextPipe::execute)
                          .flatMap(Optional::stream)
                          .toList();
        };
    }

    /**
     * Connects the current pipeline to the specified next pipeline, forcing the connection even if the current pipeline has no results.
     *
     * @param nextPipe the next pipeline to connect to
     * @param <NEW_END> the type of the end result of the next pipeline
     * @return a new branched pipeline that connects the current pipeline to the specified next pipeline
     */
    default <NEW_END> BranchedPipeline<BEGIN, NEW_END> forceConnect(BranchedPipeline<? super END, NEW_END> nextPipe) {
        return (input) -> {
            List<END> results = execute(input);
            if (results.isEmpty()) {
                return nextPipe.execute(null);
            }
            return results.stream()
                          .map(nextPipe::execute)
                          .reduce(new ArrayList<>(), (acc, list) -> {
                              acc.addAll(list);
                              return acc;
                          });
        };
    }

    /**
     * Joins the branches of the pipeline using the specified joiner.
     *
     * @param joiner the binary operator used to join the branches
     * @return a new pipeline that represents the joined branches
     */
    default Pipeline<BEGIN, END> join(BinaryOperator<END> joiner) {
        return (input) -> execute().stream().reduce(joiner);
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
                       .reduce(new ArrayList<>(), (acc, list) -> {
                           acc.addAll(list);
                           return acc;
                       });
    }

    /**
     * Executes the pipeline asynchronously
     *
     * @param obj Input element
     * @return {@code CompletableFuture<List<END>>}
     */
    default CompletableFuture<List<END>> executeAsync(BEGIN obj) {
        return CompletableFuture.supplyAsync(() -> execute(obj));
    }
    
    /**
     * Executes the pipeline asynchronously without an input element
     *
     * @return {@code CompletableFuture<List<END>>}
     */
    default CompletableFuture<List<END>> executeAsync() {
        return CompletableFuture.supplyAsync(() -> execute());
    }

    /**
     * The builder interface for creating a branched pipeline.
     *
     * @param <BEGIN> the type of the input to the pipeline
     * @param <END> the type of the output from the pipeline
     */
    public interface Builder<BEGIN, END> {

        /**
         * Sets the value to be given as the output of the pipeline.
         *
         * @param value the value to be given as the output
         * @return the builder instance
         */
        Builder<BEGIN, END> give(END value);

        /**
         * Sets the collection of values to be given as the output of the pipeline.
         *
         * @param values the collection of values to be given as the output
         * @return the builder instance
         */
        Builder<BEGIN, END> give(Collection<END> values);

        /**
         * Sets the supplier of the value to be given as the output of the pipeline.
         *
         * @param supplier the supplier of the value to be given as the output
         * @return the builder instance
         */
        Builder<BEGIN, END> give(Supplier<END> supplier);

        /**
         * Filters the output of the pipeline using the specified predicate.
         *
         * @param filter the predicate used to filter the output
         * @return the builder instance
         */
        Builder<BEGIN, END> filter(Predicate<END> filter);

        /**
         * Filters the elements of the pipeline based on the given predicate and maps them to a new type using the provided function.
         *
         * @param filter the predicate used to filter the elements
         * @param ifTrue the function used to map the filtered elements to a new type
         * @param <NEW_END> the type of the new elements in the pipeline
         * @return a new Builder instance with the filtered and mapped elements
         */
        <NEW_END> Builder <BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue);
        
        /**
         * Filters the elements of the pipeline based on the given predicate and maps them to a new type.
         *
         * @param filter  the predicate used to filter the elements
         * @param ifTrue  the function to apply to elements that satisfy the predicate
         * @param ifFalse the function to apply to elements that do not satisfy the predicate
         * @param <NEW_END> the type of the elements in the resulting pipeline
         * @return a new Builder instance with the filtered and mapped elements
         */
        <NEW_END> Builder <BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue, Function<END, NEW_END> ifFalse);

        /**
         * Filters the pipeline process based on the given filter predicate.
         * If the filter predicate evaluates to true, the provided consumer is executed.
         *
         * @param filter the predicate used to filter the pipeline process
         * @param ifTrue the consumer to be executed if the filter predicate evaluates to true
         * @return the updated pipeline builder
         */
        Builder <BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue);

        /**
         * Filters the elements in the pipeline based on the given predicate and performs
         * different actions depending on whether the predicate is true or false.
         *
         * @param filter  the predicate used to filter the elements
         * @param ifTrue  the consumer to be executed for elements that pass the filter
         * @param ifFalse the consumer to be executed for elements that do not pass the filter
         * @return the builder instance with the filter process added
         */
        Builder <BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse);

        /**
         * Joins the output of the pipeline with another pipeline using the specified binary operator.
         *
         * @param joiner the binary operator used to join the output
         * @return the builder instance
         */
        Pipeline.Builder<BEGIN, END> join(BinaryOperator<END> joiner);

        /**
         * Maps the output of the pipeline to a new type using the specified mapper function.
         *
         * @param mapper the mapper function used to map the output
         * @param <NEW_END> the type of the new output
         * @return the builder instance
         */
        <NEW_END> Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper);

        /**
         * Pipes the output of the pipeline to another optional pipeline.
         *
         * @param nextPipe the optional pipeline to pipe the output to
         * @param <NEW_END> the type of the output from the next pipeline
         * @return the builder instance
         */
        <NEW_END> Builder<BEGIN, NEW_END> pipe(OptionalPipeline<? super END, NEW_END> nextPipe);

        /**
         * Pipes the output of the pipeline to another branched pipeline.
         *
         * @param nextPipe the branched pipeline to pipe the output to
         * @param <NEW_END> the type of the output from the next pipeline
         * @return the builder instance
         */
        <NEW_END> Builder<BEGIN, NEW_END> pipe(BranchedPipeline<? super END, NEW_END> nextPipe);

        /**
         * Processes the output of the pipeline using the specified consumer.
         *
         * @param processor the consumer used to process the output
         * @return the builder instance
         */
        Builder<BEGIN, END> process(Consumer<END> processor);

        /**
         * Runs the specified runnable after the pipeline is built.
         *
         * @param runnable the runnable to be run
         * @return the builder instance
         */
        Builder<BEGIN, END> run(Runnable runnable);

        /**
         * Builds the branched pipeline.
         *
         * @return the built branched pipeline
         */
        BranchedPipeline<BEGIN, END> build();

    }
    
}
