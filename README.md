# camel-operon

Operon-component for Apache Camel

https://operon.io

"Operon is a programming language which can be easily embedded with JVM-based 
languages (Java, Scala, Groovy, Kotlin, Clojure, etc.) or run standalone 
with the native-version. Operon is especially great for transforming and 
handling JSON-data."

https://operon.io/components/running-operon-from-apache-camel

### Usage with Maven

```
  <dependency>
    <groupId>io.operon</groupId>
    <artifactId>camel-operon</artifactId>
    <version>0.9.7.1-RELEASE</version>
  </dependency>
```

### Options

* queryFile : java.lang.String. When starts with "file://" then tries to load the query from the file-system, otherwise from the classpath.
* outputResult : boolean (default false). When set, the Operon will output result to System.out
* prettyPrint : boolean (default false). When set, the result will be formatted.
* printDuration : boolean (default false). When set, the query execution time will be print to System.out
* debug : boolean (default false). When set, some debug-information is printed during the component execution.
* timezone : java.lang.String. When set, the default-timezone will be overridden for date-functions.

### Headers

Please note that the headers are case-insensitive, so it does not matter whether we type INITIALVALUE or initialValue.
The headers are listed in the class CamelOperonHeaders and it is encouraged to use these instead of hard-coding the values.

* initialValue: allows to set the root-value ($) for the query.
	- CamelOperonHeaders.HEADER_INITIAL_VALUE

* operonModules: loads any external Operon-scripts as libraries before executing the script.
	- CamelOperonHeaders.HEADER_OPERON_MODULES

* operonConfigs: the `io.operon.runner.model.OperonConfigs` -instance to manually conrol e.g. "outputResult" when using Camel's language-expression (which is there by default set to false).
    - CamelOperonHeaders.HEADER_OPERON_CONFIGS

* operonValueBindings: allows to bind values to the query.
	- CamelOperonHeaders.HEADER_OPERON_VALUE_BINDINGS

* operonScript: the operon-script can be set into this header
	- CamelOperonHeaders.HEADER_LANGUAGE_SCRIPT

* operonScriptPath: the path from where the script is found
    - CamelOperonHeaders.HEADER_LANGUAGE_SCRIPT_PATH

* inputMimeType: decides how to interpret the input. Allowed values: application/json (default), application/java.
	- CamelOperonHeaders.HEADER_INPUT_MIME_TYPE
	- When "application/json", then parses the input as Operon-value.
	- When "application/java", then parses the input as Operon-value from Java-object.
	- When "application/octet-stream", then expects the input to be byte[], and parses the input as Operon-value: RawValue.

* outputMimeType: decides how to interpret the output. Allowed values: application/json (default), application/java, application/java-list, application/octet-stream
	- CamelOperonHeaders.HEADER_OUTPUT_MIME_TYPE
	- When "application/json", then serializes the result as Java String, representing the JSON.
	- When "application/java", then keeps the result as Operon typed value, e.g. NumberType.
	- When "application/java-list", then pulls out the Java Lists from the Operon typed values. This allows using the Camel's Split-EIP with Operon-expression. ArrayType gives List<Node>, ObjectType gives List<ObjectType>, and other types are plainly put into a List<OperonValue>.
	- When "application/octet-stream", then expected result is RawValue, from which the byte-array is taken.

* content_type: sets both the input and output mime-type, if they are not set. Allowed values: application/json, application/java, application/java-list (inputMimeType will be set as application/java), application/octet-stream.
	- CamelOperonHeaders.HEADER_CONTENT_TYPE

### Component-examples

```
import io.operon.camel.model.CamelOperonHeaders;

@Override
protected RouteBuilder createRouteBuilder() throws Exception {
  return new RouteBuilder() {
    public void configure() {
    
    	//
    	// Query without initial-value
    	//
	    from("timer://t1?repeatCount=1")
	      .setBody(constant("Select: 123"))
	      .to("operon://bar")
	      ;
	      
    	//
    	// Output-result to console and pretty-print it:
    	//
	    from("timer://t1?repeatCount=1")
	      .setBody(constant("Select {outputResult, prettyPrint}: 123"))
	      .to("operon://bar")
	      ;
	
		//
		// Query with initial-value
		//
	    from("timer://t1?repeatCount=1")
	      //
	      // Another way to set the header is: .setHeader(CamelOperonHeaders.HEADER_INITIAL_VALUE, constant("{\"foo\": \"bar\"}"))
	      // which requires first importing io.operon.camel.model.CamelOperonHeaders.
	      //
	      .setHeader("initialValue", constant("{\"foo\": \"bar\"}"))
	      .setBody(constant("Select: $.foo"))
	      .to("operon://bar")
	      ;
	    
	    //
	    // Run two queries, the second query takes input the first query's output
	    //
	    from("timer://t2?repeatCount=10")
	      .setBody(constant("Select: -> http:{\"url\": \"https://api.chucknorris.io/jokes/random\"}"))
	      .to("operon://quotes1")
	      .log("Setting initial value :: " + simple("${body}"))
	      .setHeader("initialValue", simple("${body}"))
	      .setBody(constant("Select: $.body.value"))
	      .to("operon://quotes2")
	      ;
	    
	    //
	    // Load query from classpath
	    //
	    from("timer://t3?repeatCount=1")
	      .to("operon://foo?queryFile=queries/q.op")
	      ;
	    
	    //
	    // Load query from file
	    //
	    from("timer://t4?repeatCount=1")
	      .to("operon://foo?queryFile=file://./src/test/resources/queries/q.op")
	      ;
	    
	    //
	    // Modules can be given in a comma-separated list.
	    // 
	    from("timer://t5?repeatCount=1")
	      .setHeader("OPERON_MODULES", constant("mylib:lib/counter.opm"))
	      .setHeader("initialValue", constant("[1,2,3]"))
	      .setBody(constant("Select: $ => mylib:count()"))
	      .to("operon://bar")
	      ;
	
		//
		// Bind values
		//
	    Map<String, String> valueBindings = new HashMap<String, String>();
	    valueBindings.put("foo", "\"bar\"");
	    valueBindings.put("bin", "100");
	
	    from("timer://t6?repeatCount=1")
	      .setHeader("OPERON_VALUE_BINDINGS", constant(valueBindings))
	      .setBody(constant("Select: $foo"))
	      .to("operon://bar")
	      ;
	}
  }
}
```

### Language-examples

```
public class Foo {
    private String name = "Bar";
    private Map<String, String> bins;
    public Foo() {
        this.bins = new HashMap<String, String>();
        this.bins.put("bin", "bai");
        this.bins.put("baa", "baba");
        
    }
}

@Override
protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {
        public void configure() {
            //
            // Query with the script
            //
            from("direct://start")
              .doTry()
                .setBody().language("operon", "Select: 123")
              .doCatch(Exception.class)
				.log("ERROR OCCURED :: ${exception}")
              .end();
            
            //
            // Load the script from the header
            //
            from("direct://start2")
              .setHeader("operonScript", constant("Select: 123"));
              .setBody().language("operon", null)
              ;
            
	        
	        //
	        // Almost any Java-object is automatically converted into JSON, which
	        // can be queried.
	        // The CONTENT_TYPE must be set to "application/java" for automatic-conversion
	        // (Note that this also sets the output-mime-type). You can also only set the input-mime-type, 
	        // and the output-mime-type would remain as application/json.
	        //
	        Foo foo = new Foo();
	        
            from("direct://start3")
              .setHeader(CamelOperonHeaders.HEADER_LANGUAGE_SCRIPT, constant("Select: $"))
              .setHeader(CamelOperonHeaders.HEADER_CONTENT_TYPE, constant("application/java"))
              .setBody().constant(foo) // NOTE: this refers to value foo of type Foo, defined above.
              .setBody().language("operon", null)
              ;
            
            //
            // Query with initial value
            //
            from("direct://start4")
              .setHeader("initialValue").constant("222")
              .setBody()
                .language("operon", "Select: $")
              ;
                
            //
            // Splitter-example
            //
            from("direct://start5")
              .setHeader(CamelOperonHeaders.HEADER_OUTPUT_MIME_TYPE)
                .constant(CamelOperonMimeTypes.MIME_APPLICATION_JAVA_LIST)
              
              .setHeader(CamelOperonHeaders.HEADER_INITIAL_VALUE)
                .constant("{foo: [10, 20, 30]}")
              
              .split()
                .language("operon", "Select: $.foo")
                .to("direct:handleSplitValue")
              .end()
              ;
              
            //
            // Modules and value-bindings can be used as with the components
            //
        }
    };
}
```
