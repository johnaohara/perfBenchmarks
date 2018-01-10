Reflective lambda invocation benchmark
======================================
TLDR;
-----

If direct method invocation is not possible, a ``static final`` MethodHandle provides invocation performance comparable to a direct method call. 
Method invocation using an injected Lambda function (``LambdaMetafactory.metafactory(...).getTarget().invokeExact()``) is also an efficient method of reflectively invoking methods.
If the MethodHandle is not declared as ``static final``, there is a large performance penalty. 

Intro
----
There are multiple methodologies to reflectively call functions in java classes, but if performance is critical, which is the best method to use?

This microbenchmark explores three different reflective invocation methods and compares invocation throughput for the difference invocation types vs direct method invocation.

* Reflection: ``Object.class.getDeclaredMethod().reflected.invoke()``
* Method Handler: ``MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() ).invoke()``
* Method Handler invokeExact: ``MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() ).invokeExact()``
* Static Final Method Handler: ``static final methodHandle = MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() );  methodHandle.invoke()``
* Static Final Method Handler invokeExact: ``static final methodHandle = MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() );  methodHandle.invokeExact()``
* Lambda: ``LambdaMetafactory.metafactory(...).getTarget().invokeExact()``

Using LambdaMetafactory.metafactory(), It is possible to inject a lamdba functional interface into a class at runtime that makes a delegated function invocation.
  
The purpose of this benchmark is to determine the overhead of each of the reflective method invocation strategies compared to a direct method call.

This benchmark has been developed using JMH: <http://openjdk.java.net/projects/code-tools/jmh/>

Results
--------

````
$java -jar ./target/benchmarks.jar

Benchmark                                            Mode  Cnt          Score         Error  Units
MyBenchmark.testDirect                              thrpt  200  368421789.854 ± 3363086.477  ops/s
MyBenchmark.testStaticFinalMethodHandleInvoke       thrpt  200  369643294.491 ± 3309676.967  ops/s
MyBenchmark.testStaticFinalMethodHandleInvokeExact  thrpt  200  368751057.847 ± 3719232.802  ops/s
MyBenchmark.testLambda                              thrpt  200  369292125.806 ± 3347804.787  ops/s
MyBenchmark.testMethodHandle                        thrpt  200  119828121.068 ±  140291.267  ops/s
MyBenchmark.testMethodHandleInvokeExact             thrpt  200  119908454.932 ±  109235.814  ops/s
MyBenchmark.testReflection                          thrpt  200   19155581.695 ± 5932398.677  ops/s
````
Profiling
--------

Looking further into the data using the LinuxPerfNormProfiler shipped with JMH, we can see;

````
$java -jar ./target/benchmarks.jar  -prof perfnorm

Benchmark                                                               Mode  Cnt          Score         Error  Units
MyBenchmark.testDirect:cycles:u                                        thrpt    2          9.015                  #/op
MyBenchmark.testDirect:instructions:u                                  thrpt    2         30.528                  #/op

MyBenchmark.testStaticFinalMethodHandleInvoke:cycles:u                 thrpt    2          8.852                  #/op
MyBenchmark.testStaticFinalMethodHandleInvoke:instructions:u           thrpt    2         30.640                  #/op

MyBenchmark.testStaticFinalMethodHandleInvokeExact:cycles:u            thrpt    2          8.865                  #/op
MyBenchmark.testStaticFinalMethodHandleInvokeExact:instructions:u      thrpt    2         30.675                  #/op

MyBenchmark.testLambda:cycles:u                                        thrpt    2          8.874                  #/op
MyBenchmark.testLambda:instructions:u                                  thrpt    2         30.624                  #/op

MyBenchmark.testMethodHandle:cycles:u                                  thrpt    2         19.053                  #/op
MyBenchmark.testMethodHandle:instructions:u                            thrpt    2         60.762                  #/op

MyBenchmark.testMethodHandleInvokeExact:cycles:u                       thrpt    2         18.812                  #/op
MyBenchmark.testMethodHandleInvokeExact:instructions:u                 thrpt    2         60.851                  #/op

MyBenchmark.testReflection:cycles:u                                    thrpt    2         56.394                  #/op
MyBenchmark.testReflection:instructions:u                              thrpt    2        164.565                  #/op
````

Conclusion
----------

Reflection requires 5.39 times more cpu instructions per invocation compared to a direct method call, whereas a static final MethodHandle and an injected lambda require approximately the same number of cpu instructions to invoke the reflected method compared to a direct method call.

This increase in number of instructions and cpu cycles is reflected in the throughputs recorded by the benchmark

Method reflection using a static final MethodHandle or an injected Lambda functions is the most efficient method of reflectively invoking methods if direct method invocation is not possible.

If the MethodHandle is not declared as ``static final``, there is a performance penalty and throughput drops by 67.4% compared a MethodHandle declared as ``static final``   
 
Build
-----

To build this benchmark;

`` $mvn clean package``

Run
---

``$java -jar ./target/benchmarks.jar``
