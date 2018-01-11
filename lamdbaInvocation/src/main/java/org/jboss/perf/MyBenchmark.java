package org.jboss.perf;

import org.jboss.perf.model.Company;
import org.jboss.perf.model.Person;
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
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

public class MyBenchmark {

   @State(Scope.Thread)
   public static class BenchmarkState {

      public SimpleBean simpleBean;

      public int count = 0;
      public static final Method reflected;
      public static final MethodHandles.Lookup methodHandlesLookup;
      public static final MethodHandle staticMethodHandle;
      public MethodHandle methodHandle;
      public static final IntBinaryOperator lambda;

      public Function<Person, String> personGetter;
      public Function<Company, String> companyLambda;

      private Person person;
      private Company company;

      static {
         MethodHandle initializeMH = null;
         Method initializeReflected = null;
         IntBinaryOperator reflectedLambda = null;

         methodHandlesLookup = MethodHandles.lookup();

         try {
            initializeReflected = SimpleBean.class.getDeclaredMethod( "myMethod", int.class, int.class );
            initializeMH = methodHandlesLookup.unreflect( initializeReflected );

            CallSite site = LambdaMetafactory.metafactory(
                methodHandlesLookup, "applyAsInt", MethodType.methodType( IntBinaryOperator.class ),
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
         staticMethodHandle = initializeMH;
      }

      @Setup(Level.Trial)
      public void setup() throws Throwable {

         simpleBean = new SimpleBean();
         methodHandle = methodHandlesLookup.unreflect( reflected );

         personGetter = buildGetterLambda( String.class, Person.class, "getName" );
         companyLambda = buildGetterLambda( String.class, Company.class, "getName" );

         person = new Person("bob");
         company = new Company("Red Hat");

      }
   }

   @Benchmark
   public int testMethodHandle(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.methodHandle.invoke( 1000, benchmarkState.count  );
   }

   @Benchmark
   public int testMethodHandleInvokeExact(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.methodHandle.invokeExact( 1000, benchmarkState.count  );
   }

   @Benchmark
   public int testStaticFinalMethodHandleInvoke(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.staticMethodHandle.invoke( 1000, benchmarkState.count  );
   }

   @Benchmark
   public int testStaticFinalMethodHandleInvokeExact(BenchmarkState benchmarkState) throws Throwable {
      benchmarkState.count++;
      return (int) benchmarkState.staticMethodHandle.invokeExact( 1000, benchmarkState.count  );
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

   @Benchmark
   public String testPersonLambda(BenchmarkState benchmarkState) {
      benchmarkState.count++;
      benchmarkState.personGetter.apply( benchmarkState.person );
      return benchmarkState.personGetter.apply( benchmarkState.person );
   }

   @Benchmark
   public String testPolyMorphoicLambda(BenchmarkState benchmarkState) {
      benchmarkState.count++;
      benchmarkState.personGetter.apply( benchmarkState.person );
      return benchmarkState.companyLambda.apply( benchmarkState.company );
   }

   static <T, R> Function<T, R> buildGetterLambda(Class<R> returnType, Class<T> objType, String gettername) throws Throwable{

      final MethodHandles.Lookup lookup = MethodHandles.lookup();

      final CallSite site = LambdaMetafactory.metafactory(lookup,
          "apply",
          MethodType.methodType(Function.class),
          MethodType.methodType(Object.class, Object.class),
          lookup.findVirtual(objType, gettername, MethodType.methodType(returnType)),
          MethodType.methodType(returnType, objType));

      return (Function) site.getTarget().invokeExact();

   }
}
