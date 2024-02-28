package tcpassos.pipeline.classes;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import tcpassos.pipeline.BasePipeline;
import tcpassos.pipeline.BasePipeline.Builder;
import tcpassos.pipeline.MergeablePipeline;
import tcpassos.pipeline.Pipeline;

public class BranchedPipelineBuilderImpl <BEGIN, END, P extends BasePipeline<BEGIN, ?>, P_END> implements BasePipeline.Builder<BEGIN, END> {

    private final MergeablePipeline<BEGIN, END, P, P_END> pipeline;

    public BranchedPipelineBuilderImpl(MergeablePipeline<BEGIN, END, P, P_END> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public Builder<BEGIN, END> give(END value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'give'");
    }

    @Override
    public Builder<BEGIN, END> give(Supplier<END> supplier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'give'");
    }

    @Override
    public Builder<BEGIN, END> filter(Predicate<END> filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filter'");
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterMap'");
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue,
            Function<END, NEW_END> ifFalse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterMap'");
    }

    @Override
    public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterProcess'");
    }

    @Override
    public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filterProcess'");
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'map'");
    }

    @Override
    public <NEW_END> Builder<BEGIN, NEW_END> pipe(Pipeline<? super END, NEW_END> nextPipe) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pipe'");
    }

    @Override
    public Builder<BEGIN, END> process(Consumer<END> processor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

    @Override
    public Builder<BEGIN, END> run(Runnable runnable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    @Override
    public BasePipeline<BEGIN, ?> build() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
