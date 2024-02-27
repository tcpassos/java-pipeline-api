package tcpassos.pipeline;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A pipeline that can be atomically replaced.
 *
 * @param <BEGIN> The input type of the pipeline.
 * @param <END>   The output type of the pipeline.
 */
public class AtomicPipeline<BEGIN, END> implements Pipeline<BEGIN, END> {

    /* Reference to the pipeline that will be executed. */
    private final AtomicReference<Pipeline<? super BEGIN, END>> pipelineReference;

    /**
     * Creates a new atomic pipeline with an empty pipeline.
     */
    public AtomicPipeline() {
        this.pipelineReference = new AtomicReference<>(obj -> Optional.empty());
    }

    /**
     * Creates a new atomic pipeline with the given pipeline.
     *
     * @param pipeline The pipeline to be executed.
     */
    public AtomicPipeline(Pipeline<? super BEGIN, END> pipeline) {
        this.pipelineReference = new AtomicReference<>(pipeline);
    }

    /**
     * Atomically sets the pipeline that will be executed.
     *
     * @param newPipeline The new pipeline to be executed.
     */
    public void set(Pipeline<BEGIN, END> newPipeline) {
        pipelineReference.set(newPipeline);
    }

    /**
     * Executes the pipeline that is currently set.
     * 
     * @param input The input to the pipeline.
     */
    @Override
    public Optional<END> execute(BEGIN input) {
        return pipelineReference.get().execute(input);
    }

}
