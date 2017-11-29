package anagrams;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JMHBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHBenchmark.class.getSimpleName() + ".*")
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void noop() {
    }
}
