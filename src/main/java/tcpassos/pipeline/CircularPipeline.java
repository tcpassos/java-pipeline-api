package tcpassos.pipeline;

import java.util.Optional;

/**
 * Represents a circular pipeline that processes elements of type T and returns an optional result of type T.
 * The circular pipeline executes a base pipeline repeatedly until it returns an empty optional.
 *
 * @param <T> the type of elements processed by the pipeline
 */
public interface CircularPipeline<T> extends BasePipeline<T, Optional<T>> {

    /**
     * Creates a circular pipeline from a base pipeline.
     *
     * @param pipeline the base pipeline to be executed repeatedly
     * @param <T> the type of elements processed by the pipeline
     * @return a circular pipeline that executes the base pipeline repeatedly until it returns an empty optional
     */
    static <T> CircularPipeline<T> of(BasePipeline<T, Optional<T>> pipeline) {
        return (input) -> {
            Optional<T> result;
            while ((result = pipeline.execute(input)).isPresent()) {
                input = result.get();
            }
            return Optional.empty();
        };
    }
}
