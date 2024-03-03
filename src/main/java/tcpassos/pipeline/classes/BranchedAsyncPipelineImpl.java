package tcpassos.pipeline.classes;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import tcpassos.pipeline.AsyncPipeline;
import tcpassos.pipeline.Pipeline;

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

    public BranchedAsyncPipelineImpl(AsyncPipeline<BEGIN, MIDDLE> original, Collection<AsyncPipeline<MIDDLE, END>> branches) {
        this.original = original;
        this.branches = branches;
    }

    @Override
    public <NEW_P_END> AsyncPipeline.Branched<BEGIN, NEW_P_END> connect(Pipeline<? super END, NEW_P_END> next) {
        var nextBranches = branches.stream()
                                .map(p -> p.connect(next))
                                .toList();
        return new BranchedAsyncPipelineImpl<>(original, nextBranches);
    }

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
