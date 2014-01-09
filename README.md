AspectJ collection
================

All aspects are used in following [Example](src/test/java/example/Example.java)


``` java
public class Example {

	private String important;

	public Example(@NullCheck String important) {
		this.important = important;
	}

	@Logged
	public String myMethod(String param, int number) {
		System.out.println("Nasty forgotten sysout: " + number);
		return important + " " + param;
	}

	public static void main(String[] args) {
		new Example("something").myMethod("works", 5);
		new Example(null); //IllegalArgumentException
	}

}
```

Execution will output
```
18:35:05.115 [main] TRACE example.Example - >>myMethod(works,5)>>
18:35:05.116 [main] WARN  net.anthavio.aspect.ApiPolicyAspect - PrintStream java.lang.System.out acces at Example.java:24
Nasty forgotten sysout: 5
18:35:05.116 [main] TRACE example.Example - <<myMethod: something works<< 1ms
Exception in thread "main" java.lang.IllegalArgumentException: Null String argument on position 1 of constructor example.Example(String)
	at net.anthavio.aspect.NullCheckAspect.checkConstructorArguments(NullCheckAspect.java:135)
	at example.Example.<init>(Example.java:18)
	at example.Example.main(Example.java:30)
```

You can see entries for
* (LogAspect) step into myMethod with parameter value
* (ApiPolicyAspect) warning about nasty System.out.println usage
* (LogAspect) exit from myMethod with return value and execution time
* (NullCheckAspect) IllegalArgumentException because of null argument passed to Example class constructor


[LogAspect](src/main/java/net/anthavio/aspect/LogAspect.java)
----------------
Yes! Yet another logging aspect, but this is quite advanced one.
On debug log level prints parameter value, but on info level prints only parameter type. 
Works nicely with Hibernate lazy loaded fields. @Logged markin annotation can be placed on method, constructor or even class to log all nonprivate methods.

[NullCheckAspect](src/main/java/net/anthavio/aspect/NullCheckAspect.java)
----------------
Checks @NullCheck annotated parameters and throws IllegalArgumentException when null value is found. Saves you lots of lines of code!

[ApiPolicyAspect](src/main/java/net/anthavio/aspect/ApiPolicyAspect.java)
----------------
Helps you to locate bad APIs usage.
Find occurences of System.out, Throwable.printStackTrace, Runtime.exit,... calls and prints warning with class and code line.


How to incorporate in you project
----------------

**Add dependency**

``` xml
	<dependency>
		<groupId>net.anthavio</groupId>
		<artifactId>anthavio-aspects</artifactId>
		<version>1.0.0</version>
	</dependency>
```

**Configure aspectj-maven-plugin to compile with anthavio-aspects library**

``` xml
  <build>
    <plugins>
  		<plugin>
  			<groupId>org.codehaus.mojo</groupId>
  			<artifactId>aspectj-maven-plugin</artifactId>
  			<executions>
  				<execution>
  					<goals>
  						<goal>compile</goal>
  					</goals>
  				</execution>
  			</executions>
  			<configuration>
  				<aspectLibraries>
  					<aspectLibrary>
  						<groupId>net.anthavio</groupId>
  						<artifactId>anthavio-aspects</artifactId>
  					</aspectLibrary>
  				</aspectLibraries>
  			</configuration>
  		</plugin>
		</plugins>
  </build>
```
