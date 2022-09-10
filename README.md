# Purpose
ApiModels is used for Generic Runtime models, that can parse various formats, and exported to various formats.

Currently, we support the following formats:

* JSON
* XML
* CSV
* Segmented Fixed Width (EDI)
* YAML

# Example Usage
```java
public class HttpHelper {
	public static JsonObjectWriter jsonWriter = new JsonObjectWriter(true);
	public static JsonObjectParser jsonParser = new JsonObjectParser(true);

    public static String parseObjectToJson(ApiObject subject) {
        try {
            return jsonWriter.writeSingle(subject);
        } catch (ApiException aex) {
            log.error("parseObjectToJson", aex);
            return null;
        }
    }

    public static ApiObject parseString(String subject) {
        try {
            return jsonParser.parseSingle(new StringReader(subject));
        } catch (ApiException | ApiClassNotFoundException aex) {
            log.error("parseString", aex);
            return null;
        }
    }
}
```

# Main Projects
The main projects of ApiModels is in 3 different repos.  Depending on the requirements for your project, you may only need ApiCore, or ApiCore and ApiTransforms.

## ApiCore
This is where the main objects of ApiModels reside.  This includes the ApiObject class itself, as well as the ApiClass and ApiClasses objects.  This create the core of the ApiModels system.
```xml
<dependency>
	<groupId>com.progbits.api</groupId>
	<artifactId>ApiCore</artifactId>
	<version>1.3.2</version>
</dependency>
```

## ApiTransforms
The Transforms project includes various types of transforms that can be used on an ApiObject.  This is where the main formats are handled.

```xml
<dependency>
	<groupId>com.progbits.api.transforms</groupId>
	<artifactId>ApiTransforms</artifactId>
	<version>1.3.7</version>
</dependency>
```

## ApiUtils
This project adds some functions for using Models with ApiObjects.  This is where you can create model definitions, and process at runtime.

```xml
<dependency>
	<groupId>com.progbits.api.util</groupId>
    <artifactId>ApiUtils</artifactId>
    <version>1.3.4</version>
</dependency>
```

# Karaf Installation
[Karaf Installation](https://github.com/kscrr73/ApiModels/docs/Karaf Install.md)
