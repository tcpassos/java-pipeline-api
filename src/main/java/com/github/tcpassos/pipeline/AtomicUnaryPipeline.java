package com.github.tcpassos.pipeline;

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

}
