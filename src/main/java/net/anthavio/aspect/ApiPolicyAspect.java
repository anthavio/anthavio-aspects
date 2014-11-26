package net.anthavio.aspect;

import java.security.AccessControlException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author martin.vanek
 *
 */
@Aspect
@SuppressAjWarnings({ "adviceDidNotMatch" })
public class ApiPolicyAspect {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static boolean killSwitch;

	/**
	 * java -DApiPolicyAspect.killSwitch=true com.something.MainClass
	 */
	static {
		String killProperty = System.getProperty("ApiPolicyAspect.killSwitch");
		if ("true".equals(killProperty)) {
			killSwitch = true;
		} else {
			killSwitch = false; //well just to be explicitly clear what is default value...
		}
	}

	public static void setKillSwitch(boolean killSwitch) {
		ApiPolicyAspect.killSwitch = killSwitch;
	}

	static final String pcSystemExit = "(call(* java.lang.System.exit(int)) || call(* java.lang.Runtime.exit(int)))";

	static final String pcRuntimeHalt = "call(* java.lang.Runtime.halt(int))";

	static final String pcPrintStackTrace = "(call(* java.lang.Throwable.printStackTrace()))";

	static final String pcNotApiOverride = "!(withincode(@net.anthavio.aspect.ApiPolicyOverride * *(..)) || within(@net.anthavio.aspect.ApiPolicyOverride *))";

	@Pointcut(pcNotApiOverride)
	public void notApiOverrideFlag() {
	}

	@Pointcut(pcSystemExit)
	public void callSystemExit() {
	}

	@Pointcut(pcRuntimeHalt)
	public void callRuntimeHalt() {
	}

	@Pointcut(pcPrintStackTrace)
	public void printStackTraceCall() {
	}

	@DeclareWarning(pcPrintStackTrace + "&& " + pcNotApiOverride)
	static final String PrintStackTraceMessage = "Throwable.printStackTrace() call from {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	//If DeclareWarning refers pointcut via method name - callSystemExit() then StackOverflowError on ajc test - nasty bug...
	@DeclareWarning(pcSystemExit + " && " + pcNotApiOverride)
	static final String systemExitMessage = "System.exit() call from {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@DeclareWarning(pcRuntimeHalt + " && " + pcNotApiOverride)
	static final String runtimeHaltMessage = "Runtime.halt() call from {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@Before("(callRuntimeHalt() || callSystemExit()) && notApiOverrideFlag()")
	public void adviceRuntimeHaltSystemExit(JoinPoint.EnclosingStaticPart esp, JoinPoint jp) {
		//log before killFlag may force to disable System.exit()
		log.warn(jp.getSignature() + " used at " + jp.getSourceLocation());

		if (killSwitch) {
			throw new AccessControlException(jp.getSignature() + " used at " + jp.getSourceLocation());
		}
	}

	@DeclareWarning("get(* System.out) && " + pcNotApiOverride)
	static final String systemOutMessage = "System.out used at {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@DeclareWarning("get(* System.err) && " + pcNotApiOverride)
	static final String systemErrMessage = "System.err used at {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@Before(value = "(get(* System.out) || get(* System.err)) && notApiOverrideFlag()")
	public void adviceSystemErrOut(JoinPoint.EnclosingStaticPart esp, JoinPoint jp) {

		if (killSwitch) {
			throw new AccessControlException(jp.getSignature() + " used at " + jp.getSourceLocation());
		}
		log.warn(jp.getSignature() + " used at " + jp.getSourceLocation());
	}

}
