Reflective lambda invocation benchmark
======================================
Into
----
There are multiple methods to reflectively call functions in java classes, but if performance is critical, which is the best method to use?

This microbenchmark explores the different reflective invocation methods and compares invocation throughput for the difference invocation types vs direct method invocation.

Using LambdaMetafactory.metafactory, It is possible to inject a lamdba functional interface into a class at runtime that makes a delegated function invocation via invokeDynamic.
  
The purpose of this benchmark is to determine the overhead of using a lambda functional interface compared to invoking a function directly or via a reflective method invocation.

Further exploration of reflective lambda possibilities can be found here:

This benchmark has been developed using JMH <http://JMH>

Findings
--------



Build
-----

To build this benchmark;

`` $mvn clean package``

Run
---

``$java -jar ./target/benchmarks.jar``