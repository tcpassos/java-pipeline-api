package tcpassos.pipeline;

import java.util.Collection;
import java.util.Optional;

/**
 * A pipeline that can be atomically replaced.
 * 
 * @param <T> The input/output type of the pipeline.
 */
public class AtomicUnaryPipeline<T> extends AtomicPipeline<T,T> implements UnaryPipeline<T> {

    /**
     * Creates a new atomic pipeline with an empty pipeline.
     */
    public AtomicUnaryPipeline() {
        super();
    }
    
    /**
     * Creates a new atomic pipeline with the given pipeline.
     *
     * @param pipeline The pipeline to be executed.
     */
    public AtomicUnaryPipeline(UnaryPipeline<T> pipeline) {
        super(pipeline);
    }

    @Override
    public <NEW_END> BranchedPipeline<T, NEW_END> connect(BranchedPipeline<? super T, NEW_END> nextPipe) {
        return UnaryPipeline.super.connect(nextPipe);
    }

    @Override
    public <NEW_END> BranchedPipeline<T, NEW_END> connect(Collection<BasePipeline<? super T, Optional<NEW_END>>> nextPipes) {
        return UnaryPipeline.super.connect(nextPipes);
    }

}
