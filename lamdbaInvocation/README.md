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
* Lambda: ``LambdaMetafactory.metafactory(...).getTarget().invokeExact()``

Using LambdaMetafactory.metafactory(), It is possible to inject a lamdba functional interface into a class at runtime that makes a delegated function invocation via invokeDynamic.
  
The purpose of this benchmark is to determine the overhead of each of the reflective method invocation strategies compared to a direct method call.

This benchmark has been developed using JMH: <http://openjdk.java.net/projects/code-tools/jmh/>

Results
--------

````
$java -jar ./target/benchmarks.jar

Benchmark                    Mode  Cnt          Score         Error  Units
MyBenchmark.testDirect      thrpt  200  387338280.397 ± 2670093.195  ops/s
MyBenchmark.testLambda      thrpt  200  374518231.277 ± 1923816.488  ops/s
MyBenchmark.testMH          thrpt  200  171491253.683 ±  634211.430  ops/s
MyBenchmark.testReflection  thrpt  200   78089006.137 ±  455278.123  ops/s
````
Findings
--------

Looking further into the data using the LinuxPerfNormProfiler shipped with JMH, we can see;

````
$java -jar ./target/benchmarks.jar  -prof perfnorm

Benchmark                                            Mode  Cnt          Score        Error  Units
MyBenchmark.testDirect:cycles:u                     thrpt   10          9.519 ±      0.058   #/op
MyBenchmark.testDirect:instructions:u               thrpt   10         31.162 ±      0.043   #/op
...
MyBenchmark.testLambda:cycles:u                     thrpt   10          9.898 ±      0.051   #/op
MyBenchmark.testLambda:instructions:u               thrpt   10         34.182 ±      0.033   #/op
...
MyBenchmark.testMH:cycles:u                         thrpt   10         21.300 ±      0.086   #/op
MyBenchmark.testMH:instructions:u                   thrpt   10         62.336 ±      0.080   #/op
...
MyBenchmark.testReflection:cycles:u                 thrpt   10         46.798 ±      0.764   #/op
MyBenchmark.testReflection:instructions:u           thrpt   10        147.204 ±      1.104   #/op
````

Reflection requires 4.92 times more cpu cycles per invocation compared to a direct method call, whereas an injected lambda require a 1.03 times more cpu cyles to invoke the reflected method.

This increase in number of instructions and cpu cycles is reflected in the throughputs recorded by the benchmark

Method reflection using injected Lambda functions is the most efficient method of reflectively invoking methods if direct method invocation is not possible.
 
Build
-----

To build this benchmark;

`` $mvn clean package``

Run
---

``$java -jar ./target/benchmarks.jar``
