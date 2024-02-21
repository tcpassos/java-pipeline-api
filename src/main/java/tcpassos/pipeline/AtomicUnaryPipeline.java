package tcpassos.pipeline;

/**
 * A pipeline that can be atomically replaced.
 * 
 * @param <T> The input/output type of the pipeline.
 */
public class AtomicUnaryPipeline<T> extends AtomicPipeline<T,T> implements UnaryPipeline<T> {

    public AtomicUnaryPipeline() {
        super();
    }
    
    public AtomicUnaryPipeline(UnaryPipeline<T> pipeline) {
        super(pipeline);
    }

}
