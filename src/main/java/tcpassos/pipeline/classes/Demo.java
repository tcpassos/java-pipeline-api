package tcpassos.pipeline.classes;

import tcpassos.pipeline.Pipeline;

public class Demo {

    public static void main(String[] args) {
        Pipeline.<String>builder()
        .give("Hello")
        .fork(b -> b.give("World"))
        .merge((s1, s2) -> s1 + " " + s2)
        .process(System.out::println)
        .build()
        .execute();
    }

    private static void sleep() {
        try { Thread.sleep(2000); } catch (InterruptedException e) { }
    }

}
