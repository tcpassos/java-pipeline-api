package tcpassos.pipeline;

import java.util.function.BinaryOperator;

/**
 * Represents a pipeline that can be joined with other pipelines.
 *
 * @param <P> the type of the base pipeline
 * @param <P_END> the type of the end result of the base pipeline
 * @param <BEGIN> the type of the beginning result
 * @param <END> the type of the end result
 */
public interface MergeablePipeline <BEGIN, END, P extends BasePipeline<BEGIN, ?>, P_END> extends BasePipeline<BEGIN, END> {

    /**
     * Connects the current pipeline with the specified next pipeline.
     *
     * @param next The next pipeline to connect.
     * @param <NEW_P_END> The type of the end result of the next pipeline.
     * @return A new pipeline with the specified next pipeline connected.
     */
    <NEW_P_END> MergeablePipeline<BEGIN, ?, ?, NEW_P_END> connect(Pipeline<? super P_END, NEW_P_END> next);

    /**
     * Joins multiple branch pipelines into a single pipeline using the specified joiner function.
     *
     * @param joiner the binary operator used to join the branch pipelines
     * @return the joined pipeline
     */
    P merge(BinaryOperator<P_END> joiner);
    
}