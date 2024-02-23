# Java Pipeline API
This library provides a comprehensive API for building pipelines in Java. Pipelines allow you to process data in a modular and efficient way, chaining different processing steps into a single flow. The API supports both linear and branched pipelines, with features for:

* Connecting steps: Combine different operations into a single pipeline.
* Data transformation: Apply functions to modify the data at each step.
* Data filtering: Remove unwanted elements from the pipeline.
* Pipeline branching: Execute different sub-pipelines in parallel.
* Asynchronous processing: Improve performance by executing pipelines asynchronously.

[See the API Javadoc](https://tcpassos.github.io/java-pipeline-api/pipeline/tcpassos/pipeline/package-summary.html)

## Usage Examples

### Example 1: Simple Linear Pipeline
``` java
Pipeline<String, Integer> pipeline = Pipes.mapping(String::length);
int result = pipeline.execute("Hello World!");
System.out.println(result); // 13
```

### Example 1: Using the builder to build pipelines
``` java
var  pipeline  =  Pipeline.<String>builder()
	.filter(s  ->  s.length() <  6)
	.map(s  ->  "Hello "  +  s)
	.process(System.out::println)
	.build();

pipeline.execute("World"); // Hello World
pipeline.executeBatch(List.of("World", "Java", "Pipeline")); // [Hello World, Hello Java]
```

### Example 3: Connecting pipelines
``` java
// Equivalent: pipeline1 = (obj) -> "Hello World"
Pipeline<String, String> pipeline1  =  Pipes.giving("Hello World");
// Equivalent: pipeline2 = (str) -> str.length()
Pipeline<String, Integer> pipeline2  =  Pipes.mapping(String::length);
Optional<Integer> strLength  =  pipeline1.connect(pipeline2).execute();
```

### Example 4: Branching
``` java
BranchedPipeline<String, String> pipeline = Pipeline.<String>builder()
    .give("Input")
    .fork(
		branch1 -> branch1.map(String::toUpperCase)
		branch2 -> branch1.map(String::toLowerCase)
	)
    .join((s1, s2) -> s1 + ", " + s2)
    .build();

System.out.println(pipeline.execute()); // INPUT, input
```

### Example 5: Asynchronous Processing
``` java
CompletableFuture<Integer> future = Pipes.mapping(String::length).executeAsync("Hello World!");
int result = future.get();
System.out.println(result); // 13
```
