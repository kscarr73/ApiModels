# Purpose

ApiModels is used for Generic Runtime models, that can parse various formats, and exported to various formats.

Currently, we support the following formats:

* JSON
* XML
* CSV
* Segmented Fixed Width (EDI)
* YAML

# Installation

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
install -s mvn:org.codehaus.woodstox/stax2-api/4.2.1
install -s mvn:com.fasterxml/aalto-xml/1.3.0
install -s mvn:com.fasterxml.jackson.core/jackson-core/2.12.3
install -s mvn:org.yaml/snakeyaml/1.29
install -s mvn:org.apache.commons/commons-csv/1.8
install -s mvn:com.progbits.api/ApiCore/1.2.5
install -s mvn:com.progbits.api.transforms/ApiTransforms/1.3.2
install -s mvn:com.progbits.api.util.mapping.graalvm/ApiMappingGraalVM/1.3.0
install -s mvn:com.progbits.api.elastic/ElasticUtils/1.0.2
feature:install jdbc
install -s mvn:org.javassist/javassist/3.19.0-GA
install -s mvn:org.mariadb.jdbc/mariadb-java-client/2.7.1
install mvn:com.zaxxer/HikariCPMariaDBFragment/1.0.0
install -s mvn:com.zaxxer/HikariCP/3.4.5
install -s mvn:com.progbits.db/OsgiDatabase/1.3.0
install -s mvn:com.progbits.db/OsgiDbCommands/1.2.0
install -s mvn:com.progbits.db/SsDbUtils/2.3.10
install -s mvn:com.progbits.api.util/ApiUtils/1.3.1
install -s mvn:com.progbits.api.utils.db/ApiUtilsDb/1.0.2
```

## These should no longer be needed
```
#install -s mvn:org.glassfish/javax.json/1.1.4
#install -s mvn:org.javolution/javolution-core-java/6.0.0
```

* Now that you have the ApiModels base, you can install the ApiWeb.  Reference <https://github.com/kscarr73/ApiModels/blob/master/ApiWeb/README.md>