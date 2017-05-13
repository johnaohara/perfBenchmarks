package org.jboss.perf;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;

public class MyBenchmark {

   @State(Scope.Thread)
   public static class BenchmarkState {


      public SimpleBean simpleBean;

      public int count = 0;
      public Method reflected;
      public MethodHandles.Lookup lookup;
      public MethodHandle mh;
      public IntBinaryOperator lambda;

      @Setup(Level.Trial)
      public void setup() throws Throwable {

         simpleBean = new SimpleBean();

         lookup = MethodHandles.lookup();

         reflected = SimpleBean.class.getDeclaredMethod( "myMethod", int.class, int.class );
         MethodHandle target = lookup.unreflect( reflected );

         mh = target;

         CallSite site = LambdaMetafactory.metafactory(
            lookup, "applyAsInt", MethodType.methodType( IntBinaryOperator.class ),
            mh.type(), mh, mh.type() );

         MethodHandle factory = site.getTarget();

         lambda = (IntBinaryOperator) factory.invokeExact();

      }
   }

   @Benchmark
   public int testMH(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.mh.invoke( 1000, benchmarkState.count  );
      return benchmarkState.count;
   }

   @Benchmark
   public int testReflection(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.reflected.invoke( benchmarkState.simpleBean, 1000, benchmarkState.count );
      return benchmarkState.count;
   }

   @Benchmark
   public int testDirect(BenchmarkState benchmarkState) {
      benchmarkState.count += benchmarkState.simpleBean.myMethod( 1000, benchmarkState.count );
      benchmarkState.simpleBean.parse( new Object() );
      return benchmarkState.count;
   }

   @Benchmark
   public int testLambda(BenchmarkState benchmarkState) {
      benchmarkState.count += benchmarkState.lambda.applyAsInt( 1000, benchmarkState.count );
      return benchmarkState.count;
   }

}
