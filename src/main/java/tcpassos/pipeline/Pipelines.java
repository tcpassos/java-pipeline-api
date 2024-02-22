package tcpassos.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import tcpassos.pipeline.BranchedPipeline.Builder;

/**
 * Utility class for pipeline operations
 */
final class Pipelines {

    private Pipelines() {}

    static final class PipelineBuilderImpl <BEGIN, END> implements Pipeline.Builder<BEGIN, END> {

        private final Pipeline<BEGIN, END> pipeline;

        PipelineBuilderImpl(Pipeline<BEGIN, END> pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public Pipeline.Builder<BEGIN, END> give(END obj) {
            return new PipelineBuilderImpl<>(pipeline.forceConnect(Pipes.giving(obj)));
        }

        @Override
        public Pipeline.Builder<BEGIN, END> give(Supplier<END> supplier) {
            return new PipelineBuilderImpl<>(pipeline.forceConnect(Pipes.giving(supplier)));
        }

        @Override
        public Pipeline.Builder<BEGIN, END> filter(Predicate<END> filter) {
            return new PipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter)));
        }
        
        @Override
        public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue) {
            Pipeline<END, NEW_END> pipe = Pipes.filtering(filter).connect(Pipes.mapping(ifTrue));
            return new PipelineBuilderImpl<>(pipeline.connect(pipe));
        }

        @Override
        public <NEW_END> Pipeline.Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue, Function<END, NEW_END> ifFalse) {
            Pipeline<BEGIN, NEW_END> pipe = (input) -> pipeline.execute(input)
                .flatMap(result -> filter.test(result) ? Optional.ofNullable(ifTrue.apply(result)) : Optional.ofNullable(ifFalse.apply(result)));
            return new PipelineBuilderImpl<>(pipe);
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
        @SuppressWarnings("unchecked")
        public <NEW_END> BranchedPipeline.Builder<BEGIN, NEW_END> fork(Function<Pipeline.Builder<BEGIN, END>, Pipeline.Builder<BEGIN, NEW_END>> ... branches) {
            BranchedPipeline<BEGIN, NEW_END> pipe = (input) -> Arrays.stream(branches)
                .map(f -> f.apply(this).build())
                .flatMap(p -> p.execute(input).stream())
                .toList();
            return BranchedPipeline.builder(pipe);
        }

        @Override
        public <NEW_END> BranchedPipeline.Builder<BEGIN, NEW_END> fork(Collection<OptionalPipeline<END, NEW_END>> pipelines) {
            BranchedPipeline<BEGIN, NEW_END> pipe = (input) -> pipeline.execute(input)
                .map(result -> pipelines.stream()
                    .map(p -> p.execute(result))
                    .flatMap(Optional::stream)
                    .toList())
                .orElse(Collections.emptyList());
            return BranchedPipeline.builder(pipe);
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
        public <NEW_END> Builder<BEGIN, NEW_END> pipe(BranchedPipeline<? super END, NEW_END> nextPipe) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(nextPipe));
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

    static final class UnaryPipelineBuilderImpl <T> implements UnaryPipeline.Builder<T> {

        private final UnaryPipeline<T> pipeline;

        UnaryPipelineBuilderImpl(UnaryPipeline<T> pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public UnaryPipeline.Builder<T> give(T obj) {
            return new UnaryPipelineBuilderImpl<>(pipeline.forceConnect(UnaryPipes.giving(obj)));
        }

        @Override
        public UnaryPipeline.Builder<T> give(Supplier<T> supplier) {
            return new UnaryPipelineBuilderImpl<>(pipeline.forceConnect(UnaryPipes.giving(supplier)));
        }

        @Override
        public UnaryPipeline.Builder<T> filter(Predicate<T> filter) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.filtering(filter)));
        }

        @Override
        public UnaryPipeline.Builder<T> filterMap(Predicate<T> filter, Function<T, T> ifTrue) {
            UnaryPipeline<T> pipe = pipeline.connect(UnaryPipes.filtering(filter).connect(UnaryPipes.mapping(ifTrue)));
            return new UnaryPipelineBuilderImpl<>(pipe);            
        }

        @Override
        public UnaryPipeline.Builder<T> filterMap(Predicate<T> filter, Function<T, T> ifTrue, Function<T, T> ifFalse) {
            UnaryPipeline<T> pipe = (input) -> pipeline.execute(input)
                .flatMap(result -> filter.test(result) ? Optional.ofNullable(ifTrue.apply(result)) : Optional.ofNullable(ifFalse.apply(result)));
            return new UnaryPipelineBuilderImpl<>(pipe);
        }

        @Override
        public UnaryPipeline.Builder<T> filterProcess(Predicate<T> filter, Consumer<T> ifTrue) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.filtering(filter).connect(UnaryPipes.processing(ifTrue))));
        }

        @Override
        public UnaryPipeline.Builder<T> filterProcess(Predicate<T> filter, Consumer<T> ifTrue, Consumer<T> ifFalse) {
            UnaryPipeline<T> pipe = (input) -> {
                Optional<T> result = pipeline.execute(input);
                result.ifPresent(r -> (filter.test(r) ? ifTrue : ifFalse).accept(r));
                return result;
            };
            return new UnaryPipelineBuilderImpl<>(pipe);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BranchedPipeline.Builder<T, T> fork(Function<UnaryPipeline.Builder<T>, UnaryPipeline.Builder<T>>... branches) {
            BranchedPipeline<T, T> pipe = (input) -> Arrays.stream(branches)
                .map(f -> f.apply(this).build())
                .flatMap(p -> p.execute(input).stream())
                .toList();
            return BranchedPipeline.builder(pipe);
        }
    
        @Override
        public <NEW_END> Builder<T, NEW_END> fork(Collection<OptionalPipeline<T, NEW_END>> pipelines) {
            BranchedPipeline<T, NEW_END> pipe = (input) -> pipeline.execute(input)
                .map(result -> pipelines.stream()
                    .map(p -> p.execute(result))
                    .flatMap(Optional::stream)
                    .toList())
                .orElse(Collections.emptyList());
            return BranchedPipeline.builder(pipe);
        }

        @Override
        public UnaryPipeline.Builder<T> map(UnaryOperator<T> mapper) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.mapping(mapper)));
        }

        @Override
        public UnaryPipeline.Builder<T> pipe(OptionalPipeline<? super T, T> nextPipe) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(nextPipe));
        }

        @Override
        public <NEW_END> Builder<T, NEW_END> pipe(BranchedPipeline<? super T, NEW_END> nextPipe) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(nextPipe));
        }

        @Override
        public UnaryPipeline.Builder<T> process(Consumer<T> processor) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.processing(processor)));
        }

        @Override
        public UnaryPipeline.Builder<T> run(Runnable runnable) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.running(runnable)));
        }

        @Override
        public UnaryPipeline<T> build() {
            return pipeline;
        }

    }

    static final class BranchedPipelineBuilderImpl <BEGIN, END> implements BranchedPipeline.Builder<BEGIN, END> {

        private final BranchedPipeline<BEGIN, END> pipeline;

        BranchedPipelineBuilderImpl(BranchedPipeline<BEGIN, END> pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public Builder<BEGIN, END> give(END value) {
            return new BranchedPipelineBuilderImpl<>(pipeline.forceConnect(Pipes.giving(value)));
        }

        @Override
        public Builder<BEGIN, END> give(Collection<END> values) {
            BranchedPipeline<END, END> pipe = (input) -> new ArrayList<>(values);
            return new BranchedPipelineBuilderImpl<>(pipeline.forceConnect(pipe));
        }

        @Override
        public Builder<BEGIN, END> give(Supplier<END> supplier) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.giving(supplier)));
        }

        @Override
        public Builder<BEGIN, END> filter(Predicate<END> filter) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter)));
        }

        @Override
        public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter).connect(Pipes.mapping(ifTrue))));
        }

        @Override
        public <NEW_END> Builder<BEGIN, NEW_END> filterMap(Predicate<END> filter, Function<END, NEW_END> ifTrue, Function<END, NEW_END> ifFalse) {
            BranchedPipeline<BEGIN, NEW_END> pipe = (input) -> pipeline.execute(input).stream()
                .map(result -> filter.test(result) ? ifTrue.apply(result) : ifFalse.apply(result)).toList();
            return new BranchedPipelineBuilderImpl<>(pipe);
        }

        @Override
        public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.filtering(filter).connect(Pipes.processing(ifTrue))));
        }

        @Override
        public Builder<BEGIN, END> filterProcess(Predicate<END> filter, Consumer<END> ifTrue, Consumer<END> ifFalse) {
            BranchedPipeline<BEGIN, END> pipe = (input) -> {
                List<END> result = pipeline.execute(input);
                result.forEach(r -> (filter.test(r) ? ifTrue : ifFalse).accept(r));
                return result;
            };
            return new BranchedPipelineBuilderImpl<>(pipe);
        }

        @Override
        public Pipeline.Builder<BEGIN, END> join(BinaryOperator<END> joiner) {
            return new PipelineBuilderImpl<>(pipeline.join(joiner));
        }

        @Override
        public <NEW_END> Builder<BEGIN, NEW_END> map(Function<? super END, NEW_END> mapper) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(Pipes.mapping(mapper)));
        }

        @Override
        public <NEW_END> Builder<BEGIN, NEW_END> pipe(OptionalPipeline<? super END, NEW_END> nextPipe) {
            return new BranchedPipelineBuilderImpl<>(pipeline.connect(nextPipe));
        }

        @Override
        public <NEW_END> Builder<BEGIN, NEW_END> pipe(BranchedPipeline<? super END, NEW_END> nextPipe) {
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

    }

}