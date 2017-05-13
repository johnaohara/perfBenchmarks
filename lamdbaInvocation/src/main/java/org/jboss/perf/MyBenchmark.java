/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

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

         //define specification - using Consumer functional interface
         lookup = MethodHandles.lookup();

         reflected = SimpleBean.class.getDeclaredMethod( "myMethod", int.class, int.class );
         MethodHandle target = lookup.unreflect( reflected );

         mh = target;

         CallSite site = LambdaMetafactory.metafactory(
            lookup, "applyAsInt", MethodType.methodType( IntBinaryOperator.class ),
            mh.type(), mh, mh.type() );

         MethodHandle factory = site.getTarget();

         // bind to instance
//         factory = factory.bindTo( simpleBean );
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
