Purpose
=======

Installation
============

# Install

* This package uses a modified Karaf installation.  
You can download this version from here.  <https://github.com/kscarr73/ApiModels/blob/master/docs/progbits-karaf-4.3.0.7z>

The changes are listed here. <https://github.com/kscarr73/ApiModels/blob/master/docs/Karaf.md>

* Start Karaf
* Make sure you have configured your ElasticSearch Environment. Reference <https://github.com/kscarr73/ElasticUtils/blob/main/README.md>

* In the Karaf Console, paste the following:

```
feature:install http-whiteboard
feature:install scr
install -s mvn:org.glassfish/javax.json/1.1.4
install -s mvn:org.javolution/javolution-core-java/6.0.0
install -s mvn:org.apache.commons/commons-csv/1.8
install -s mvn:com.progbits.api/ApiCore/1.1.0
install -s mvn:com.progbits.api.transforms/ApiTransforms/1.1.0
install -s mvn:com.progbits.api.util.mapping.graalvm/ApiMappingGraalVM/1.1.0
install -s mvn:com.progbits.api.elastic/ElasticUtils/1.0.1
install -s mvn:com.progbits.api.util/ApiUtils/1.1.2
```

* Now that you have the ApiModels base, you can install the ApiWeb.  Reference <https://github.com/kscarr73/ApiModels/blob/master/ApiWeb/README.md>