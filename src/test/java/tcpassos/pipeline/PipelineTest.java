package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class PipelineTest {

    @Test
    public void emptyTest() {
        Pipeline<String, String> pipelineStr = Pipeline.empty();
        Pipeline<Integer, Integer> pipelineInt = Pipeline.empty();
        assertEquals(Optional.of("Test"), pipelineStr.execute("Test"));
        assertEquals(Optional.of(123), pipelineInt.execute(123));
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
    public void processingTest() {
        AtomicBoolean booleanValue = new AtomicBoolean();
        Pipeline<AtomicBoolean, AtomicBoolean> pipeline = Pipes.processing(obj -> obj.set(true));
        assertTrue(pipeline.execute(booleanValue).get().get());
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

    @Test
    public void streamOfTest() {
        Pipeline<String, String> pipeline = Pipes.mapping((String str) -> str.toUpperCase())
                                                    .connect(Pipes.filtering(str -> str.contains("TEST")));
        Stream<String> stream = pipeline.streamOf(List.of("Test 1", "test 2", "Error", "TEST 3"));
        List<String> actualList = stream.collect(Collectors.toList());
        List<String> expectedList = Arrays.asList("TEST 1", "TEST 2", "TEST 3");
        assertEquals(3, actualList.size());
        assertTrue(actualList.containsAll(expectedList));
    }

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

}