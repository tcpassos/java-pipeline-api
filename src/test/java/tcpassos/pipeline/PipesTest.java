package tcpassos.pipeline;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class PipesTest {

    @Test
    void testGivingWithValue() {
        String value = "Hello";
        Pipeline<Void, String> pipeline = Pipes.giving(value);
        Optional<String> result = pipeline.execute();
        assertEquals(value, result.get());
    }

    @Test
    void testGivingWithSupplier() {
        Supplier<Integer> supplier = () -> 42;
        Pipeline<Void, Integer> pipeline = Pipes.giving(supplier);
        Optional<Integer> result = pipeline.execute();
        assertEquals(42, result.get());
    }

    @Test
    void testProcessing() {
        StringBuilder sb = new StringBuilder();
        Consumer<StringBuilder> processor = (str) -> str.append(" World");
        Pipeline<StringBuilder, StringBuilder> pipeline = Pipes.processing(processor);
        Optional<StringBuilder> result = pipeline.execute(sb);
        assertEquals(" World", result.get().toString());
    }

    @Test
    void testRunning() {
        StringBuilder sb = new StringBuilder();
        Runnable runnable = () -> sb.append("Hello");
        Pipeline<StringBuilder, StringBuilder> pipeline = Pipes.running(runnable);
        Optional<StringBuilder> result = pipeline.execute(sb);
        assertEquals("Hello", result.get().toString());
    }

    @Test
    void testFiltering() {
        Predicate<Integer> filter = (num) -> num % 2 == 0;
        Pipeline<Integer, Integer> pipeline = Pipes.filtering(filter);
        Optional<Integer> result1 = pipeline.execute(5);
        Optional<Integer> result2 = pipeline.execute(10);
        assertFalse(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(10, result2.get());
    }

    @Test
    void testMapping() {
        Function<String, Integer> mapper = (str) -> str.length();
        Pipeline<String, Integer> pipeline = Pipes.mapping(mapper);
        Optional<Integer> result = pipeline.execute("Hello");
        assertEquals(5, result.get());
    }
}