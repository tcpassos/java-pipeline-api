package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

class UnaryPipesTest {

    @Test
    void testProcessing() {
        StringBuilder sb = new StringBuilder();
        Consumer<StringBuilder> processor = (str) -> str.append(" World");
        UnaryPipeline<StringBuilder> pipeline = UnaryPipes.processing(processor);
        Optional<StringBuilder> result = pipeline.execute(sb);
        assertEquals(" World", result.get().toString());
    }

    @Test
    void testRunning() {
        StringBuilder sb = new StringBuilder();
        Runnable runnable = () -> sb.append("Hello");
        UnaryPipeline<StringBuilder> pipeline = UnaryPipes.running(runnable);
        Optional<StringBuilder> result = pipeline.execute(sb);
        assertEquals("Hello", result.get().toString());
    }

    @Test
    void testFiltering() {
        Predicate<Integer> filter = (num) -> num % 2 == 0;
        UnaryPipeline<Integer> pipeline = UnaryPipes.filtering(filter);
        Optional<Integer> result1 = pipeline.execute(5);
        Optional<Integer> result2 = pipeline.execute(10);
        assertFalse(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(10, result2.get());
    }

    @Test
    void testMapping() {
        UnaryOperator<String> mapper = (str) -> str + " World";
        UnaryPipeline<String> pipeline = UnaryPipes.mapping(mapper);
        Optional<String> result = pipeline.execute("Hello");
        assertEquals("Hello World", result.get());
    }
}