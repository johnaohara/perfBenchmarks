package org.jboss.perf;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
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
      public static final Method reflected;
      public static final MethodHandles.Lookup lookup;
      public static final MethodHandle mh;
      public static final IntBinaryOperator lambda;

      static {
         MethodHandle initializeMH = null;
         Method initializeReflected = null;
         IntBinaryOperator reflectedLambda = null;

         lookup = MethodHandles.lookup();

         try {
            initializeReflected = SimpleBean.class.getDeclaredMethod( "myMethod", int.class, int.class );
            initializeMH = lookup.unreflect( initializeReflected );

            CallSite site = LambdaMetafactory.metafactory(
                lookup, "applyAsInt", MethodType.methodType( IntBinaryOperator.class ),
                initializeMH.type(), initializeMH, initializeMH.type() );

            MethodHandle factory = site.getTarget();

            reflectedLambda = (IntBinaryOperator) factory.invokeExact();


         } catch (NoSuchMethodException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         } catch (LambdaConversionException e) {
            e.printStackTrace();
         } catch (Throwable throwable) {
            throwable.printStackTrace();
         }

         lambda = reflectedLambda;
         reflected = initializeReflected;
         mh = initializeMH;
      }

      @Setup(Level.Trial)
      public void setup() throws Throwable {

         simpleBean = new SimpleBean();

      }
   }

   @Benchmark
   public int testMH(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.mh.invoke( 1000, benchmarkState.count  );
   }

   @Benchmark
   public int testMHinvokeExact(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.mh.invokeExact( 1000, benchmarkState.count  );
   }

   @Benchmark
   public int testReflection(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.reflected.invoke( benchmarkState.simpleBean, 1000, benchmarkState.count );
   }

   @Benchmark
   public int testDirect(BenchmarkState benchmarkState) {
      benchmarkState.count++;
      return benchmarkState.simpleBean.myMethod( 1000, benchmarkState.count );
   }

   @Benchmark
   public int testLambda(BenchmarkState benchmarkState) {
      benchmarkState.count++;
      return benchmarkState.lambda.applyAsInt( 1000, benchmarkState.count );
   }
}
