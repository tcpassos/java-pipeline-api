package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class PipelineTest {

    @Test
    public void newBuilderTest() {
        Pipeline.Builder<String, String> builder = Pipeline.builder();
        AtomicBoolean triggered = new AtomicBoolean(false);
        builder.map(str -> str.concat("cd"))
                  .map(str -> str.concat("ef"))
                  .filter(str -> str.equals("abcdef"))
                  .process(str -> triggered.set(true))
                  .build()
                  .execute("ab");
        assertTrue(triggered.get());
    }

    @Test
    public void builderOfPipelineTest() {
        AtomicBoolean triggered = new AtomicBoolean(false);
        Pipeline.builder(Pipes.mapping((String str) -> str.length()))
                .map(num -> num + 5)
                .filter(num -> num == 10)
                .process(num -> triggered.set(true))
                .build()
                .execute("12345");
        assertTrue(triggered.get());
    }

    @Test
    public void emptyTest() {
        Pipeline<String, String> pipelineStr = Pipeline.empty();
        Pipeline<Integer, Integer> pipelineInt = Pipeline.empty();
        assertEquals(Optional.of("Test"), pipelineStr.execute("Test"));
        assertEquals(Optional.of(123), pipelineInt.execute(123));
    }

    @Test
    void testAsSupplier() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Supplier<Optional<Integer>> supplier = pipeline.asSupplier();
        Optional<Integer> result = supplier.get();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsNullableSupplier() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Supplier<Integer> supplier = pipeline.asNullableSupplier();
        Integer result = supplier.get();
        assertEquals(42, result);
    }

    @Test
    void testAsConsumer() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Consumer<String> consumer = pipeline.asConsumer();
        consumer.accept("Hello");
        Optional<Integer> result = pipeline.execute();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsRunnable() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Runnable runnable = pipeline.asRunnable();
        runnable.run();
        Optional<Integer> result = pipeline.execute();
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsPredicate() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Predicate<String> predicate = pipeline.asPredicate();
        assertTrue(predicate.test("Hello"));
    }

    @Test
    void testAsFunction() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Function<String, Optional<Integer>> function = pipeline.asFunction();
        Optional<Integer> result = function.apply("Hello");
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testAsNullableFunction() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Function<String, Integer> function = pipeline.asNullableFunction();
        Integer result = function.apply("Hello");
        assertEquals(42, result);
    }

    @Test
    public void connectTest() {
        Pipeline<String, String> uppercasePipeline = Pipes.mapping((str) -> str.toUpperCase());
        Pipeline<String, String> concatPipeline = Pipes.mapping((str) -> str.concat("test2"));
        assertEquals(Optional.of("TEST1"), uppercasePipeline.execute("test1"));
        assertEquals(Optional.of("test1test2"), concatPipeline.execute("test1"));
        assertEquals(Optional.of("TEST1test2"), uppercasePipeline.connect(concatPipeline).execute("test1"));
        assertEquals(Optional.of("TEST1TEST2"), concatPipeline.connect(uppercasePipeline).execute("test1"));
    }

    @Test
    public void connectBranchTest() {
        Pipeline<String, String> uppercasePipeline = Pipes.mapping((str) -> str.toUpperCase());
        Pipeline<String, String> concatPipeline1 = Pipes.mapping((str) -> str.concat("test1"));
        Pipeline<String, String> concatPipeline2 = Pipes.mapping((str) -> str.concat("test2"));
        var result = uppercasePipeline.connect(List.of(concatPipeline1, concatPipeline2)).execute("test");
        assertEquals(2 , result.size());
        assertEquals("TESTtest1", result.get(0));
        assertEquals("TESTtest2", result.get(1));

        Pipeline<String, Integer> lengthPipeline = Pipes.mapping((str) -> str.length());
        Pipeline<Integer, Integer> addPipeline = Pipes.mapping((num) -> num + 5);
        Pipeline<Integer, Integer> multiplyPipeline = Pipes.mapping((num) -> num * 2);
        var result2 = lengthPipeline.connect(List.of(addPipeline, multiplyPipeline)).execute("test");
        assertEquals(2 , result2.size());
        assertEquals(Integer.valueOf(9), result2.get(0));
        assertEquals(Integer.valueOf(8), result2.get(1));
    }

    @Test
    public void processingTest() {
        AtomicBoolean booleanValue = new AtomicBoolean();
        Pipeline<AtomicBoolean, AtomicBoolean> pipeline = Pipes.processing(obj -> obj.set(true));
        assertTrue(pipeline.execute(booleanValue).get().get());
    }

    @Test
    void testExecuteBatch() {
        Pipeline<String, Integer> pipeline = createPipeline();
        Collection<String> elements = Arrays.asList("Hello", "World");
        List<Integer> result = pipeline.executeBatch(elements);
        assertEquals(2, result.size());
        assertTrue(result.contains(42));
    }

    @Test
    public void filteringTest() {
        Pipeline<String, String> simplePipeline = Pipes.filtering((str) -> str.equals("test"));
        assertTrue(simplePipeline.execute("test").isPresent());
        assertTrue(simplePipeline.execute("abcde").isEmpty());
        Pipeline<String, Integer> chainedPipeline = Pipes.filtering((String str) -> str.contains("a"))
                                                            .connect(Pipes.filtering((String str) -> str.contains("b")))
                                                            .connect(Pipes.filtering((String str) -> str.contains("c")))
                                                            .connect(Pipes.filtering((String str) -> str.contains("d")))
                                                            .connect(Pipes.mapping((str) -> str.length()))
                                                            .connect(Pipes.filtering((num) -> num <= 4));
        assertTrue(chainedPipeline.execute("aaaa").isEmpty());
        assertTrue(chainedPipeline.execute("abcdf").isEmpty());
        assertTrue(chainedPipeline.execute("abcd").isPresent());
        assertEquals(Integer.valueOf(4), chainedPipeline.execute("abcd").get());
    }

    @Test
    public void mappingTest() {
        Pipeline<String, String> concatPipeline = Pipes.mapping(str -> str.concat(";test2"));
        Pipeline<String, String[]> splitPipeline = Pipes.mapping(str -> str.split(";"));
        String[] expectedSplitValues = {"test1", "test2"};
        assertEquals("test1;test2", concatPipeline.execute("test1").get());
        assertArrayEquals(expectedSplitValues, splitPipeline.execute("test1;test2").get());
        assertArrayEquals(expectedSplitValues, concatPipeline.connect(splitPipeline).execute("test1").get());
    }

    private Pipeline<String, Integer> createPipeline() {
        return new Pipeline<String, Integer>() {
            @Override
            public Optional<Integer> execute(String input) {
                return Optional.of(42);
            }
        };
    }

}