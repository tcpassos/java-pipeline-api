package tcpassos.pipeline;

import java.util.List;

/**
 * Represents a branched pipeline in the Java Pipeline API.
 * A branched pipeline takes an input of type BEGIN and produces a list of outputs of type END.
 * It extends the MergablePipeline interface and provides additional functionality for merging the branches.
 *
 * @param <BEGIN> the type of the input to the pipeline
 * @param <END> the type of the output from the pipeline
 */
public interface BranchedPipeline <BEGIN, END> extends MergeablePipeline<BEGIN, List<END>, Pipeline<BEGIN, END>, END> {

    public interface Builder <BEGIN, END> extends BasePipeline.Builder<BEGIN, List<END>> {
        
    }

}