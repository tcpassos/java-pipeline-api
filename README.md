
# Java Pipeline API
Java API to work with pipeline processing.

[See the API Javadoc](https://tcpassos.github.io/java-pipeline-api/pipeline/tcpassos/pipeline/package-summary.html)

## Examples
### Using the builder to build pipelines
``` java
var  pipeline  =  Pipeline.<String>builder()
						  .filter(s  ->  s.length() <  6)
						  .map(s  ->  "Hello "  +  s)
						  .process(System.out::println)
						  .build();

// This will print "Hello World"
pipeline.execute("World");

// This will print "Hello World" and "Hello Java"
// ... but not "Hello Pipeline" because it has more than 6 characters
pipeline.executeBatch(List.of("World", "Java", "Pipeline"));
```

### Connecting pipelines
``` java
// Equivalent: pipeline1 = (obj) -> "Hello World"
Pipeline<String, String> pipeline1  =  Pipes.giving("Hello World");
// Equivalent: pipeline2 = (str) -> str.length()
Pipeline<String, Integer> pipeline2  =  Pipes.mapping(String::length);
Optional<Integer> strLength  =  pipeline1.connect(pipeline2).execute();
```
