package tcpassos.pipeline;

import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Represents a branched pipeline in the Java Pipeline API.
 * A branched pipeline takes an input of type BEGIN and produces a list of outputs of type END.
 * It extends the MergablePipeline interface and provides additional functionality for merging the branches.
 *
 * @param <BEGIN> the type of the input to the pipeline
 * @param <END> the type of the output from the pipeline
 */
public interface BranchedPipeline <BEGIN, END> extends MergeablePipeline<BEGIN, List<END>, Pipeline<BEGIN, END>, END> {

    /**
     * Connects the current pipeline with the specified next pipeline.
     *
     * @param next The next pipeline to connect.
     * @param <NEW_END> The type of the end result of the next pipeline.
     * @return A new pipeline with the specified next pipeline connected.
     */
    @Override
    public <NEW_END> BranchedPipeline<BEGIN, NEW_END> connect(Pipeline<? super END, NEW_END> next);

    /**
     * Builder interface for creating a branched pipeline.
     * 
     * @param <BEGIN> the type of the input to the pipeline
     * @param <END> the type of the output from the pipeline
     */
    public interface Builder <BEGIN, END> extends BasePipeline.Builder<BEGIN, END> {

        /**
         * Merges the current pipeline with another pipeline using the specified merge function.
         *
         * @param mergeFunction the binary operator used to merge the two pipelines
         * @return a new pipeline representing the merged result
         */
        Pipeline.Builder<BEGIN, END> merge(BinaryOperator<END> mergeFunction);
        
    }

}