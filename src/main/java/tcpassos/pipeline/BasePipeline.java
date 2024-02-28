package tcpassos.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base of a pipeline, which supports the processing of an input element, returning an output element.
 *
 * @param <BEGIN> Pipeline input element type
 * @param <END> Pipeline output element type
 */
@FunctionalInterface
public interface BasePipeline <BEGIN, END> {

    /**
     * Processes an input element through the pipeline stages, returning a processed output element
     *
     * @param obj Input element
     * @return {@code END}
     */
    END execute(BEGIN obj);

    /**
     * Executes the pipeline without an input element
     *
     * @return {@code END}
     */
    default END execute() {
        return execute(null);
    }

    /**
     * The `Builder` interface represents a builder for creating pipelines in a fluent manner.
     * It provides methods for adding stages to the pipeline, such as giving input elements, filtering elements,
     * mapping elements to a new type, piping to another pipeline, processing elements, and running code.
     *
     * @param <BEGIN> the type of the input elements in the pipeline
     * @param <END> the type of the output elements in the pipeline
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
         * @return the updated pipeline Builder
         */
        Builder <BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue);

        /**
         * Filters the elements in the pipeline based on the given predicate and performs
         * different actions depending on whether the predicate is true or false.
         *
         * @param filter  the predicate used to filter the elements
         * @param ifTrue  the consumer to be executed for elements that pass the filter
         * @param ifFalse the consumer to be executed for elements that do not pass the filter
         * @return the Builder instance with the filter process added
         */
        Builder <BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse);

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
         * @return {@code BPipelineuilder<BEGIN, NEW_END>}
         */
        <NEW_END> Builder <BEGIN, NEW_END> pipe(Pipeline<? super END, NEW_END> nextPipe);

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
         * @return {@code BasePipeline<BEGIN, ?>}
         */
        BasePipeline<BEGIN, ?> build();

    }

}