package tcpassos.pipeline;

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

}