Reflective lambda invocation benchmark
======================================
TLDR;
-----

Method reflection using injected Lambda functions (``LambdaMetafactory.metafactory(...).getTarget().invokeExact()``) is the most efficient method of reflectively invoking methods if direct method invocation is not possible.

Intro
----
There are multiple methodologies to reflectively call functions in java classes, but if performance is critical, which is the best method to use?

This microbenchmark explores three different reflective invocation methods and compares invocation throughput for the difference invocation types vs direct method invocation.

* Reflection: ``Object.class.getDeclaredMethod().reflected.invoke()``
* Method Handler: ``MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() ).invoke()``
* Method Handler invokeExact: ``MethodHandles.lookup().unreflect( Object.class.getDeclaredMethod() ).invokeExact()``
* Lambda: ``LambdaMetafactory.metafactory(...).getTarget().invokeExact()``

Using LambdaMetafactory.metafactory(), It is possible to inject a lamdba functional interface into a class at runtime that makes a delegated function invocation.
  
The purpose of this benchmark is to determine the overhead of each of the reflective method invocation strategies compared to a direct method call.

This benchmark has been developed using JMH: <http://openjdk.java.net/projects/code-tools/jmh/>

Results
--------

````
$java -jar ./target/benchmarks.jar

Benchmark                       Mode  Cnt          Score         Error  Units
MyBenchmark.testDirect         thrpt  200  368003105.280 ± 3345971.929  ops/s
MyBenchmark.testLambda         thrpt  200  322230333.227 ± 1740815.058  ops/s
MyBenchmark.testMH             thrpt  200  120040071.926 ±  146106.498  ops/s
MyBenchmark.testMHinvokeExact  thrpt  200  119352133.926 ±  541080.577  ops/s
MyBenchmark.testReflection     thrpt  200   13308472.507 ± 5071938.628  ops/s
````
Findings
--------

Looking further into the data using the LinuxPerfNormProfiler shipped with JMH, we can see;

````
$java -jar ./target/benchmarks.jar  -prof perfnorm

Benchmark                                               Mode  Cnt          Score         Error  Units
MyBenchmark.testDirect:cycles                          thrpt   10         10.300 ±       0.088   #/op
MyBenchmark.testDirect:instructions                    thrpt   10         30.623 ±       0.156   #/op
...
MyBenchmark.testLambda:cycles                          thrpt   10         11.742 ±       0.039   #/op
MyBenchmark.testLambda:instructions                    thrpt   10         34.962 ±       0.047   #/op
...
MyBenchmark.testMH:cycles                              thrpt   10         31.811 ±       0.672   #/op
MyBenchmark.testMH:instructions                        thrpt   10         81.304 ±       0.081   #/op
...
MyBenchmark.testMHinvokeExact:cycles                   thrpt   10         31.748 ±       0.556   #/op
MyBenchmark.testMHinvokeExact:instructions             thrpt   10         81.255 ±       0.201   #/op
...
MyBenchmark.testReflection:cycles:u                    thrpt   10         46.798 ±      0.764   #/op
MyBenchmark.testReflection:instructions:u              thrpt   10        147.204 ±      1.104   #/op

````

Reflection requires 4.56 times more cpu cycles per invocation compared to a direct method call, whereas an injected lambda require a 1.14 times more cpu cyles to invoke the reflected method.

This increase in number of instructions and cpu cycles is reflected in the throughputs recorded by the benchmark

Method reflection using injected Lambda functions is the most efficient method of reflectively invoking methods if direct method invocation is not possible.
 
Build
-----

To build this benchmark;

`` $mvn clean package``

Run
---

``$java -jar ./target/benchmarks.jar``
