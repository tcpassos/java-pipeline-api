package tcpassos.pipeline.classes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import tcpassos.pipeline.ParallelPipeline;
import tcpassos.pipeline.Pipeline;

public class ParallelPipelineImpl <BEGIN, MIDDLE, END> implements ParallelPipeline<BEGIN, END> {

    private final Pipeline<BEGIN, MIDDLE> original;
    private final Collection<Pipeline<MIDDLE, END>> branches;

    public ParallelPipelineImpl(Pipeline<BEGIN, MIDDLE> original, Collection<Pipeline<MIDDLE, END>> branches) {
        this.original = original;
        this.branches = branches;
    }

    @Override
    public Pipeline<BEGIN, END> merge(BinaryOperator<END> joiner) {
        return (obj) -> {
            var result = original.execute(obj);
            return branches.stream()
                           .parallel()
                           .flatMap(p -> p.execute(result.get()).stream())
                           .reduce(joiner)
                           .map(Optional::of)
                           .orElse(Optional.empty());
        };
    }
    @Override
    public List<END> execute(BEGIN obj) {
        var result = original.execute(obj);
        return branches.stream()
                       .parallel()
                       .flatMap(p -> p.execute(result.get()).stream())
                       .toList();
    }
    
}