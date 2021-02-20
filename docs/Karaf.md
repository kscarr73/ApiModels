# Changes

There are a few things that need to be added to a default karaf install.

* Download the latest Apache Karaf Runtime (https://karaf.apache.org/download.html)
* Unzip and call the folder something other than apache-karaf-{version}

## Adding the GraalVM environment

* cd karaf/lib/jdk9plus
* Copy each of the following jars into the folder

```
 asm-7.3.1.jar
 asm-analysis-7.3.1.jar
 asm-commons-7.3.1.jar
 asm-tree-7.3.1.jar
 asm-util-7.3.1.jar
 graal-sdk-20.0.0.jar
 icu4j-66.1.jar
 js-20.0.0.jar
 regex-20.0.0.jar
 truffle-api-20.0.0.jar
```

These will be loaded as jdk9+ modules.

## etc changes

* cd karaf/etc
* Open jre.properties
* Locate the `jre-9=` \ Line
* Add the following above the javax.accessibility, \ line:
```
 org.graalvm.polyglot, \
 org.graalvm.polyglot.proxy, \
 sun.nio.ch, \
```
* Save the file
