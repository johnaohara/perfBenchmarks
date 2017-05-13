package org.jboss.perf;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;

/**
 * Created by johara on 09/05/17.
 */
public class BenchmarkRunner {

   public static void main(String[] args) {
      ChainedOptionsBuilder builder =
         new OptionsBuilder()
            .resultFormat( ResultFormatType.JSON )
            .result( "target/JmhResults.json" ).include( MyBenchmark.class.getSimpleName() );

      builder.forks( 0 );
      builder.warmupIterations( 1 );
      builder.measurementIterations( 1 );

      Options opt = builder.build();
      try {
         new Runner( opt ).run();
      } catch (RunnerException e) {
         e.printStackTrace();
      }

   }
}
