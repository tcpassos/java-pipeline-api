package tcpassos.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

class AsyncPipelineTest {

    @Test
    void testAsyncPipelineOf() {
        Pipeline<Void, String> pipeline = Pipes.giving("Hello");
        AsyncPipeline<Void, String> asyncPipeline = AsyncPipeline.of(pipeline);

        CompletableFuture<String> futureResult = asyncPipeline.execute(null);
        try {
            String result = futureResult.get(1, TimeUnit.SECONDS);
            assertEquals("Hello", result);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Execution should complete within 1 second");
        }
    }

    @Test
    void testAsyncPipelineConnect() {
        Pipeline<String, Integer> pipeline1 = Pipes.mapping(String::length);
        Pipeline<Integer, Boolean> pipeline2 = Pipes.mapping(num -> num % 2 == 0);

        AsyncPipeline<String, Integer> asyncPipeline1 = AsyncPipeline.of(pipeline1);
        AsyncPipeline<Integer, Boolean> asyncPipeline2 = AsyncPipeline.of(pipeline2);

        AsyncPipeline<String, Boolean> connectedPipeline = asyncPipeline1.connect(asyncPipeline2);

        CompletableFuture<Boolean> futureResult = connectedPipeline.execute("Hello");
        try {
            Boolean result = futureResult.get(1, TimeUnit.SECONDS);
            assertFalse(result);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Execution should complete within 1 second");
        }
    }

}