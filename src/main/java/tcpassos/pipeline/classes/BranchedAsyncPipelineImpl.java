package tcpassos.pipeline.classes;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import tcpassos.pipeline.AsyncPipeline;

/**
 * Represents an implementation of the {@link AsyncPipeline.Branched} interface.
 * This class allows branching of an asynchronous pipeline, where the original pipeline
 * is executed along with multiple branches, and the results are merged using a joiner.
 *
 * @param <BEGIN>  the type of the input to the original pipeline
 * @param <MIDDLE> the type of the intermediate result between the original pipeline and the branches
 * @param <END>    the type of the final result after merging the original pipeline and the branches
 */
public class BranchedAsyncPipelineImpl<BEGIN, MIDDLE, END> implements AsyncPipeline.Branched<BEGIN, END> {

    private final AsyncPipeline<BEGIN, MIDDLE> original;
    private final Collection<AsyncPipeline<MIDDLE, END>> branches;

    /**
     * Constructs a new instance of the {@code BranchedAsyncPipelineImpl} class.
     *
     * @param original the original pipeline to be executed
     * @param branches the collection of branches to be executed
     */
    public BranchedAsyncPipelineImpl(AsyncPipeline<BEGIN, MIDDLE> original, Collection<AsyncPipeline<MIDDLE, END>> branches) {
        this.original = original;
        this.branches = branches;
    }

    /**
     * Merges the original pipeline with the branches using the specified joiner.
     *
     * @param joiner the binary operator used to merge the results of the original pipeline and the branches
     * @return a new asynchronous pipeline that merges the original pipeline and the branches
     */
    @Override
    public AsyncPipeline<BEGIN, END> merge(BinaryOperator<END> joiner) {
        return (obj) -> {
            return original.execute(obj).thenCompose(res -> {
                var futures = branches.stream()
                                     .map(p -> p.execute(res))
                                     .collect(Collectors.toList());
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .thenApply(v -> futures.stream()
                                                           .map(CompletableFuture::join)
                                                           .reduce(joiner)
                                                           .orElseThrow());
            });
        };
    }

    /**
     * Executes the original pipeline and the branches, and returns a CompletableFuture
     * that completes with a list of the merged results.
     *
     * @param obj the input to the original pipeline
     * @return a CompletableFuture that completes with a list of the merged results
     */
    @Override
    public CompletableFuture<List<END>> execute(BEGIN obj) {
        return original.execute(obj).thenCompose(res -> {
            var futures = branches.stream()
                                 .map(p -> p.execute(res))
                                 .collect(Collectors.toList());
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                 .thenApply(v -> futures.stream()
                                                        .map(CompletableFuture::join)
                                                        .collect(Collectors.toList()));
        });
    }

}
