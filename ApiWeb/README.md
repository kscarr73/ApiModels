# Purpose

Implements a Web Site for creating and maintaining an ApiModel environment.

# MariaDB Service

* Create a Database called smartmapper
* Use the Database script to create the tables: <https://github.com/kscarr73/ApiModels/blob/master/ApiWeb/docs/database.sql>

# Karaf Configuration

* Create a file called datasources.cfg

```
smartMapper_Driver=org.mariadb.jdbc.Driver
smartMapper_URL=jdbc:mariadb://localhost:3306/smartmapper
smartMapper_Username={your user}
smartMapper_Password={your password}
smartMapper_Verify=true
```

# Karaf Install

* Install the OsgiDb components by copying the following in your Karaf Console.

* Copy and Paste this section into your running karaf environment

	<dependency>
			<groupId>javax.mail</groupId>
			<artifactId>javax.mail-api</artifactId>
			<version>1.4.7</version>
		</dependency>
		
		<dependency>
			<groupId>com.sun.mail</groupId>
			<artifactId>javax.mail</artifactId>
			<version>1.6.2</version>
		</dependency>


```
install -s mvn:org.apache.servicemix.specs/org.apache.servicemix.specs.activation-api-1.1/2.6.0
install -s mvn:com.sun.mail/javax.mail/1.6.2
install -s mvn:org.freemarker/freemarker/2.3.31
install -s mvn:com.progbits.web/JQueryServlet/3.6.0
install -s wrap:mvn:net.sourceforge.plantuml/plantuml/1.2021.9
install -s mvn:com.fasterxml.jackson.core/jackson-core/2.12.3
install -s mvn:com.fasterxml.jackson.core/jackson-annotations/2.12.3
install -s mvn:com.fasterxml.jackson.core/jackson-databind/2.12.3
install -s mvn:commons-codec/commons-codec/1.15
install -s wrap:mvn:com.auth0/java-jwt/3.12.0
install -s mvn:com.progbits.web/WebUtils/1.2.0
install -s mvn:com.progbits.api.web/ApiWeb/2.2.4
```

If everything is successful, you can type `http:list` to see the following:

```
ID  │ Servlet       │ Servlet-Name   │ State       │ Alias   │ Url
125 │ JQueryServlet │ ServletModel-2 │ Deployed    │ /jsjq   │ [/jsjq/*]
135 │ ApiWebServlet │ ServletModel-5 │ Deployed    │ /apiweb │ [/apiweb/*]
```
