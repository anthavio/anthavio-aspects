package com.anthavio.aspect;

import java.security.AccessControlException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@SuppressAjWarnings({ "adviceDidNotMatch" })
public class ApiPolicyAspect {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static boolean killFlag = false;

	public static void setKillKlag(boolean killFlag) {
		ApiPolicyAspect.killFlag = killFlag;
	}

	static final String pcSystemExit = "(call(* java.lang.System.exit(int)) || call(* java.lang.Runtime.exit(int)))";

	static final String pcRuntimeHalt = "call(* java.lang.Runtime.halt(int))";

	static final String pcPrintStackTrace = "(call(* java.lang.Throwable.printStackTrace()))";

	static final String pcNotApiOverride = "!(withincode(@cz.komix.aspect.annotation.ApiPolicyOverride * *(..)) || within(@cz.komix.aspect.annotation.ApiPolicyOverride *))";

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

	//DeclareWarning nesmi referencovat pointcut podle jmena metody callSystemExit()
	//jinak StackOverflowError pri ajc testu
	@DeclareWarning(pcSystemExit + " && " + pcNotApiOverride)
	static final String systemExitMessage = "System.exit() call from {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@DeclareWarning(pcRuntimeHalt + " && " + pcNotApiOverride)
	static final String runtimeHaltMessage = "Runtime.halt() call from {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@Before("(callRuntimeHalt() || callSystemExit()) && notApiOverrideFlag()")
	public void adviceRuntimeHaltSystemExit(JoinPoint.EnclosingStaticPart esp, JoinPoint jp) {
		//log before killFlag may force to disable System.exit()
		log.warn(jp.getSignature() + " acces at " + jp.getSourceLocation());

		if (killFlag) {
			throw new AccessControlException(jp.getSignature() + " acces at " + jp.getSourceLocation());
		}
	}

	@DeclareWarning("get(* System.out) && " + pcNotApiOverride)
	static final String systemOutMessage = "System.out access at {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@DeclareWarning("get(* System.err) && " + pcNotApiOverride)
	static final String systemErrMessage = "System.err access at {joinpoint.sourcelocation.sourcefile}:{joinpoint.sourcelocation.line}";

	@Before(value = "(get(* System.out) || get(* System.err)) && notApiOverrideFlag()")
	public void adviceSystemErrOut(JoinPoint.EnclosingStaticPart esp, JoinPoint jp) {

		if (killFlag) {
			throw new AccessControlException(jp.getSignature() + " acces at " + jp.getSourceLocation());
		}
		log.warn(jp.getSignature() + " acces at " + jp.getSourceLocation());
	}

}
