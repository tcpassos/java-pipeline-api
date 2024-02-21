package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtomicPipelineTest {

    private AtomicPipeline<String, Integer> atomicPipeline;

    @BeforeEach
    void setUp() {
        atomicPipeline = new AtomicPipeline<>();
    }

    @Test
    void testEmptyPipeline() {
        Optional<Integer> result = atomicPipeline.execute("Hello");
        assertFalse(result.isPresent());
    }

    @Test
    void testSetPipeline() {
        Function<String, Integer> f = Integer::parseInt;
        atomicPipeline.set(Pipes.mapping(f));
        Optional<Integer> result = atomicPipeline.execute("42");
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    void testExecutePipeline() {
        Function<String, Integer> f = Integer::parseInt;
        atomicPipeline.set(Pipes.mapping(f));
        Optional<Integer> result = atomicPipeline.execute("42");
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
        assertThrows(NumberFormatException.class, () -> atomicPipeline.execute("Hello"));
    }
}