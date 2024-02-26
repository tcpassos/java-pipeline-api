package tcpassos.pipeline.classes;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import tcpassos.pipeline.OptionalPipeline;
import tcpassos.pipeline.Pipeline;
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
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter).connect(Pipes.processing(ifTrue))));
    }

    @Override
    public Pipeline.Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse) {
        Pipeline<BEGIN, END> pipe = (input) -> {
            Optional<END> result = pipeline.execute(input);
            result.ifPresent(r -> (filter.test(r) ? ifTrue : ifFalse).accept(r));
            return result;
        };
        return new PipelineBuilderImpl<>(pipe);
    }

    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper) {
        return new PipelineBuilderImpl<>(pipeline.connect(Pipes.mapping(mapper)));
    }

    @Override
    public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> pipe(OptionalPipeline<? super END, NEW_END> nextPipe) {
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