package tcpassos.pipeline;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for pipeline operations
 */
final class Pipelines {

    private Pipelines() {}

    static final class PipelineBranchedImpl <BEGIN, MIDDLE, END> implements Pipeline.Branched<BEGIN, END> {
        
        private final Pipeline<BEGIN, MIDDLE> original;
        private final Collection<Pipeline<MIDDLE, END>> branches;

        public PipelineBranchedImpl(Pipeline<BEGIN, MIDDLE> original, Collection<Pipeline<MIDDLE, END>> branches) {
            this.original = original;
            this.branches = branches;
        }

        @Override
        public Pipeline<BEGIN, END> merge(BinaryOperator<END> joiner) {
            return (obj) -> {
                var result = original.execute(obj);
                return branches.stream()
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
                           .flatMap(p -> p.execute(result.get()).stream())
                           .collect(Collectors.toList());
        }

    }

    static final class PipelineBuilderImpl <BEGIN, END> implements Pipeline.Builder<BEGIN, END> {

        private final Pipeline<BEGIN, END> pipeline;

        PipelineBuilderImpl(Pipeline<BEGIN, END> pipeline) {
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

}