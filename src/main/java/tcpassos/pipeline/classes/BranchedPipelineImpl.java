package tcpassos.pipeline.classes;

import java.util.Collection;
import java.util.List;
import java.util.function.BinaryOperator;

import tcpassos.pipeline.BranchedPipeline;
import tcpassos.pipeline.Pipeline;

public class BranchedPipelineImpl <BEGIN, MIDDLE, END> implements BranchedPipeline<BEGIN, END> {
        
    private final Pipeline<BEGIN, MIDDLE> original;
    private final Collection<Pipeline<MIDDLE, END>> branches;

    public BranchedPipelineImpl(Pipeline<BEGIN, MIDDLE> original, Collection<Pipeline<MIDDLE, END>> branches) {
        this.original = original;
        this.branches = branches;
    }

    @Override
    public <NEW_P_END> BranchedPipeline<BEGIN, NEW_P_END> connect(Pipeline<? super END, NEW_P_END> next) {
        var nextBranches = branches.stream()
                                  .map(p -> p.connect(next))
                                  .toList();
        return new BranchedPipelineImpl<>(original, nextBranches);
    }

    @Override
    public Pipeline<BEGIN, END> merge(BinaryOperator<END> joiner) {
        return (obj) -> {
            var result = original.execute(obj);
            return result.flatMap(res -> 
                branches.stream()
                        .flatMap(p -> p.execute(res).stream())
                        .reduce(joiner)
            );
        };
    }

    @Override
    public List<END> execute(BEGIN obj) {
        var result = original.execute(obj);
        return branches.stream()
                       .flatMap(p -> p.execute(result.get()).stream())
                       .toList();
    }

}
