package tcpassos.pipeline.classes;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import tcpassos.pipeline.Pipeline;
import tcpassos.pipeline.Pipeline.Builder;
import tcpassos.pipeline.Pipes;

public class PipelineBuilderImpl <BEGIN, END> implements Pipeline.Builder<BEGIN, END> {

    private final Pipeline<BEGIN, END> pipeline;

    public PipelineBuilderImpl(Pipeline<BEGIN, END> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public Pipeline.Builder<BEGIN, END> give(END obj) {
        return new PipelineBuilderImpl<>(pipeline.connectVoid(Pipes.giving(obj)));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> give(Supplier<END> supplier) {
        return new PipelineBuilderImpl<>(pipeline.connectVoid(Pipes.giving(supplier)));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> filter(Predicate<END> filter) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter)));
    }

    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filterMapping(filter, ifTrue)));
    }
    
    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue, Function<END, NEW_END> ifFalse) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filterMapping(filter, ifTrue, ifFalse)));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filterProcessing(filter, ifTrue)));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filterProcessing(filter, ifTrue, ifFalse)));
    }

    @Override
    public <NEW_END> tcpassos.pipeline.BranchedPipeline.Builder<BEGIN, NEW_END> fork(Function<Builder<BEGIN, END>, Builder<BEGIN, NEW_END>> forkBuilderFunction) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.mapping(mapper)));
    }

    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> pipe(Pipeline<? super END, NEW_END> nextPipe) {
        return new PipelineBuilderImpl<>(pipeline.connect(nextPipe));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> process(Consumer<END> processor) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.processing(processor)));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> run(Runnable runnable) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.running(runnable)));
    }

    @Override
    public Pipeline<BEGIN, END> build() {
        return pipeline;
    }

}