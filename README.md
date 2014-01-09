AspectJ collection
================

All aspects are used in following [Example](src/test/java/net/anthavio/aspect/test/Example.java)


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
18:20:40.265 [main] TRACE net.anthavio.aspect.test.Example - >>myMethod(works,5)>>
18:20:40.266 [main] WARN  net.anthavio.aspect.ApiPolicyAspect - PrintStream java.lang.System.out acces at Example.java:23
Nasty forgotten sysout: 5
18:20:40.267 [main] TRACE net.anthavio.aspect.test.Example - <<myMethod: something works<< 1ms
Exception in thread "main" java.lang.IllegalArgumentException: Null String argument on position 1 of constructor net.anthavio.aspect.test.Example(String)
	at net.anthavio.aspect.NullCheckAspect.checkConstructorArguments(NullCheckAspect.java:135)
	at net.anthavio.aspect.test.Example.<init>(Example.java:17)
	at net.anthavio.aspect.test.Example.main(Example.java:29)
```

You can see entries for
* (LogAspect) step into myMethod with parameter value
* (ApiPolicyAspect) warning about nasty System.out.println usage
* (LogAspect) exit from myMethod with return value and execution time
* (NullCheckAspect) IllegalArgumentException because of null argument passed to Example class constructor


LogAspect
----------------
Yes! Yet another logging aspect, but this is quite advanced one.
On debug log level prints parameter value, but on info level prints only parameter type. 
Works nicely with Hibernate lazy loaded fields. @Logged markin annotation can be placed on method, constructor or even class to log all nonprivate methods.

NullCheckAspect
----------------
Checks @NullCheck annotated parameters and throws IllegalArgumentException when null value is found. Saves you lots of lines of code!

ApiPolicyAspect
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
