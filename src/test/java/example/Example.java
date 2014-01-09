package example;


import net.anthavio.aspect.Logged;
import net.anthavio.aspect.NullCheck;

/**
 * 
 * Not a test but simple showcase 
 * 
 * @author martin.vanek
 *
 */
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
