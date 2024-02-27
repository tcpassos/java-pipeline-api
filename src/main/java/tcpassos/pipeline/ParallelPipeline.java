package tcpassos.pipeline;

import java.util.List;

/**
 * Represents a parallel pipeline in the Java Pipeline API.
 * A parallel pipeline is a type of pipeline that takes a collection of input elements of type BEGIN,
 * processes them in parallel, and produces a collection of output elements of type END.
 *
 * @param <BEGIN> the type of the input elements
 * @param <END> the type of the output elements
 */
public interface ParallelPipeline <BEGIN, END> extends MergeablePipeline<BEGIN, List<END>, Pipeline<BEGIN, END>, END> { }
