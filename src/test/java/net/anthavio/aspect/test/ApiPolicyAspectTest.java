package net.anthavio.aspect.test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.security.AccessControlException;

import net.anthavio.aspect.ApiPolicyAspect;
import net.anthavio.aspect.ApiPolicyOverride;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ApiPolicyAspectTest {

	@BeforeMethod
	public void before() {
		EventStoringAppender.getEvents().clear();
		ApiPolicyAspect.setKillKlag(false);
	}

	@Test
	public void testSystemExit() {
		ApiPolicyAspect.setKillKlag(true);
		try {
			System.exit(0);
			Assert.fail("Prosel System.exit()");
		} catch (AccessControlException acx) {
			assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
		}
	}

	@Test
	public void testRuntimeHalt() {
		ApiPolicyAspect.setKillKlag(true);
		try {
			Runtime.getRuntime().halt(0);
			Assert.fail("Prosel Runtime.halt()");
		} catch (AccessControlException acx) {
			assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
		}
	}

	@Test
	public void testSystemOut() {
		System.out.println("I'm ugly System.out.println call!");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
	}

	@Test
	public void testSystemErr() {
		System.err.println("I'm ugly System.err.println() call!");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
	}

	@Test
	@ApiPolicyOverride
	public void testSystemOutMethodOverride() {
		System.out.println("I really really want this System.out.println()!");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}

	@Test
	@ApiPolicyOverride
	public void testSystemErrMethodOverride() {
		System.err.println("I really really want this System.err.println()!");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}

	@Test
	// @ApiPolicyOverride
	public void testSystemOutClassOverride() {
		new ApiPolicyOverriden().testSystemOutOverride();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}

	@Test
	public void testSystemErrClassOverride() {
		new ApiPolicyOverriden().testSystemErrOverride();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}

	public void testPrintStackTrace() {
		new NullPointerException("Test require this printStackTrace")
				.printStackTrace();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
	}

	@Test
	@ApiPolicyOverride
	public void testPrintStackTraceMethodOverride() {
		new NullPointerException("Test require this printStackTrace")
				.printStackTrace();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}

	@Test
	public void testPrintStackTraceClassOverride() {
		new ApiPolicyOverriden().testPrintStackTraceOverride();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(0);
	}
}

@ApiPolicyOverride
class ApiPolicyOverriden {

	public ApiPolicyOverriden(boolean useless) {
		System.exit(0);
	}

	public ApiPolicyOverriden() {
		System.out.println("I really really want this System.out.println()!");
		System.err.println("I really really want this System.err.println()!");
	}

	public void testSystemOutOverride() {
		System.out.println("I really really want this System.out.println()!");
	}

	public void testSystemErrOverride() {
		System.err.println("I really really want this System.err.println()!");
	}

	public void testPrintStackTraceOverride() {
		new NullPointerException("Test require this printStackTrace")
				.printStackTrace();
	}
}
