# Description

## Context

This project aims at implementing a generic, user-friendly, informative tester for Plain Old Java Objects (POJO).
It basically follows the same pobjective than [Pojo Tester](https://www.pojo.pl), except that it assumes that one should not blindly trust a testing library.

## Problem

While Pojo Tester seems interesting, it basically attempts to make a single test that tests everything at once, which results in a non-informative test report which basically says "test everything - OK".
Although it seems nice at first, it is flawed, beccause Pojo Tester does not test everything.
It tests a given set of properties, selected depending on how your class looks like, and assumes that what is tested at the end covers everything you need.
That is a strong assumption that is actually not met, since some things that may be interesting to be tested are not.
[TODO exemple]

## Solution

Instead, we provide a way to build a complete report of what is tested, which makes it easy to audit and complete the tests as needed.

# How does it Work?

The idea is to provide a set of generic parameterized tests, and to help you generate the parameters for the POJO class you want to test.
This result in a test that focuses on defining succinctly your POJO class, and running a battery of tests that corresponds to that definition.
The test report is thus a complete suite of tests that tells you what have been tested.
You can also audit each generic test, which is simple and focused, to know exactly how it has been tested.

[TODO example]

# How to use it?

If you use Maven, you can use this dependency:
(Check the last release tag for the most recent version)
```xml
<dependency>
	<groupId>fr.matthieu-vergne</groupId>
	<artifactId>pester-core</artifactId>
	<version>1.0</version>
</dependency>
```

Otherwise, you can download the JAR on the central repository of Maven or build it from the sources.
You can find examples of use in `pester-sample`.

[TODO example]
