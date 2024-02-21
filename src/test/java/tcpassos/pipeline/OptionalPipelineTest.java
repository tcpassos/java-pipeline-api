package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class OptionalPipelineTest {

    @Test
    void testAsSupplier() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Supplier<Optional<Integer>> supplier = pipeline.asSupplier();
        Optional<Integer> result = supplier.get();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsNullableSupplier() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Supplier<Integer> supplier = pipeline.asNullableSupplier();
        Integer result = supplier.get();
        assertEquals(42, result);
    }

    @Test
    void testAsConsumer() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Consumer<String> consumer = pipeline.asConsumer();
        consumer.accept("Hello");
        Optional<Integer> result = pipeline.execute();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsRunnable() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Runnable runnable = pipeline.asRunnable();
        runnable.run();
        Optional<Integer> result = pipeline.execute();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsPredicate() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Predicate<String> predicate = pipeline.asPredicate();
        assertTrue(predicate.test("Hello"));
    }

    @Test
    void testAsFunction() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Function<String, Optional<Integer>> function = pipeline.asFunction();
        Optional<Integer> result = function.apply("Hello");
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsNullableFunction() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Function<String, Integer> function = pipeline.asNullableFunction();
        Integer result = function.apply("Hello");
        assertEquals(42, result);
    }

    @Test
    void testExecuteBatch() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Collection<String> elements = Arrays.asList("Hello", "World");
        List<Integer> result = pipeline.executeBatch(elements);
        assertEquals(2, result.size());
        assertTrue(result.contains(42));
    }

    @Test
    void testExecuteAsyncWithInput() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        CompletableFuture<Integer> future = pipeline.executeAsync("Hello");
        assertEquals(42, future.join());
        assertTrue(future.isDone());
    }

    @Test
    void testExecuteAsyncWithoutInput() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        CompletableFuture<Integer> future = pipeline.executeAsync();
        assertEquals(42, future.join());
        assertTrue(future.isDone());
    }

    @Test
    void testStreamOfArray() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        String[] elements = {"Hello", "World"};
        Stream<Integer> stream = pipeline.streamOf(elements);
        List<Integer> result = stream.collect(Collectors.toList());
        assertEquals(2, result.size());
        assertTrue(result.contains(42));
    }

    @Test
    void testStreamOfIterable() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Iterable<String> elements = Arrays.asList("Hello", "World");
        Stream<Integer> stream = pipeline.streamOf(elements);
        List<Integer> result = stream.collect(Collectors.toList());
        assertEquals(2, result.size());
        assertTrue(result.contains(42));
    }

    @Test
    void testStreamOfStream() {
        OptionalPipeline<String, Integer> pipeline = createPipeline();
        Stream<String> elements = Stream.of("Hello", "World");
        Stream<Integer> stream = pipeline.streamOf(elements);
        List<Integer> result = stream.collect(Collectors.toList());
        assertEquals(2, result.size());
        assertTrue(result.contains(42));
    }

    private OptionalPipeline<String, Integer> createPipeline() {
        return new OptionalPipeline<String, Integer>() {
            @Override
            public Optional<Integer> execute(String input) {
                return Optional.of(42);
            }
        };
    }
}