package io.github.tcpassos.pipeline;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
        public UnaryPipeline.Builder<T> map(UnaryOperator<T> mapper) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(UnaryPipes.mapping(mapper)));
        }

        @Override
        public UnaryPipeline.Builder<T> pipe(OptionalPipeline<? super T, T> nextPipe) {
            return new UnaryPipelineBuilderImpl<>(pipeline.connect(nextPipe));
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

}