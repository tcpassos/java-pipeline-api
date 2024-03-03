package tcpassos.pipeline.classes;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import tcpassos.pipeline.BasePipeline;
import tcpassos.pipeline.BranchedPipeline;
import tcpassos.pipeline.BasePipeline.Builder;
import tcpassos.pipeline.Pipeline;
import tcpassos.pipeline.Pipes;

public class BranchedPipelineBuilderImpl <BEGIN, END, P extends BasePipeline<BEGIN, ?>, P_END> implements BranchedPipeline.Builder<BEGIN, END> {

    private final BranchedPipeline<BEGIN, END> pipeline;

    public BranchedPipelineBuilderImpl(BranchedPipeline<BEGIN, END> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public Builder<BEGIN, END> filter(Predicate<END> filter) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter)));
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filterMapping(filter, ifTrue)));
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue, Function<END, NEW_END> ifFalse) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filterMapping(filter, ifTrue, ifFalse)));
    }

    @Override
    public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filterProcessing(filter, ifTrue)));
    }

    @Override
    public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filterProcessing(filter, ifTrue, ifFalse)));
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.mapping(mapper)));
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> pipe(Pipeline<? super END, NEW_END> nextPipe) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(nextPipe));
    }

    @Override
    public Builder<BEGIN, END> process(Consumer<END> processor) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.processing(processor)));
    }

    @Override
    public Builder<BEGIN, END> run(Runnable runnable) {
        return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.running(runnable)));
    }

    @Override
    public BranchedPipeline<BEGIN, END> build() {
        return pipeline;
    }

    @Override
    public Pipeline.Builder<BEGIN, END> merge(BinaryOperator<END> mergeFunction) {
        return new PipelineBuilderImpl<>(pipeline.merge(mergeFunction));
    }

}
