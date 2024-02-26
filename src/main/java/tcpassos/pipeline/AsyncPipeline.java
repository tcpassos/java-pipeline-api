package tcpassos.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import tcpassos.pipeline.classes.BranchedAsyncPipelineImpl;

/**
 * Represents an asynchronous pipeline that transforms an input of type BEGIN to an output of type END.
 * The pipeline operates on CompletableFuture objects, allowing for asynchronous execution.
 *
 * @param <BEGIN> the type of the input to the pipeline
 * @param <END> the type of the output from the pipeline
 */
public interface AsyncPipeline <BEGIN, END> extends BasePipeline<BEGIN, CompletableFuture<END>> {

    /**
     * Creates an asynchronous pipeline from a base pipeline.
     *
     * @param pipeline the base pipeline to convert to an asynchronous pipeline
     * @param <BEGIN>  the type of the input to the pipeline
     * @param <END>    the type of the output from the pipeline
     * @return an asynchronous pipeline
     */
    static <BEGIN, END> AsyncPipeline<BEGIN, END> of(BasePipeline<BEGIN, Optional<END>> pipeline) {
        return (input) -> CompletableFuture.completedFuture(input)
                                          .thenApplyAsync(i -> pipeline.execute(i)
                                                                        .orElseThrow(NoSuchElementException::new));
    }

    /**
     * Connects the current pipeline to the given next pipeline.
     * 
     * @param next the next pipeline to connect to
     * @param <NEW_END> the new end type of the pipeline
     * @return a new asynchronous pipeline that represents the connection between the current pipeline and the next pipeline
     */
    default <NEW_END> AsyncPipeline <BEGIN, NEW_END> connect(BasePipeline<END, Optional<NEW_END>> next) {
        return (input) -> this.execute(input)
                              .thenCompose(result -> next.execute(result)
                                                          .map(CompletableFuture::completedFuture)
                                                          .orElseGet(() -> CompletableFuture.failedFuture(new NoSuchElementException())));
    }

    /**
     * Connects the current pipeline to the given pipeline, creating a new pipeline that executes the current pipeline
     * followed by the given pipeline.
     *
     * @param next the pipeline to connect to the current pipeline
     * @param <NEW_END> the new end type of the resulting pipeline
     * @return a new pipeline that executes the current pipeline followed by the given pipeline
     */
    default <NEW_END> AsyncPipeline <BEGIN, NEW_END> connect(AsyncPipeline<END, NEW_END> next) {
        return (input) -> this.execute(input).thenCompose(next::execute);
    }

    /**
     * Connects the current pipeline to a collection of next pipelines, creating a branched pipeline.
     * This method returns a new instance of the `Branched` interface, which represents the branched pipeline.
     * 
     * @param next the collection of next pipelines to connect to
     * @param <NEW_END> the type of the end result of the next pipelines
     * @return a new instance of the `Branched` interface representing the branched pipeline
     */
    default <NEW_END> Branched<BEGIN, NEW_END> connect(Collection<AsyncPipeline<END, NEW_END>> next) {
        return new BranchedAsyncPipelineImpl<>(this, next);
    }

    /**
     * Represents a branched asynchronous pipeline in the Java Pipeline API.
     * 
     * @param <BEGIN> the type of the input to the pipeline
     * @param <END> the type of the output from the pipeline
     */
    public interface Branched <BEGIN, END> extends MergeablePipeline<BEGIN, CompletableFuture<List<END>>, AsyncPipeline<BEGIN, END>, END> { }
    
}
