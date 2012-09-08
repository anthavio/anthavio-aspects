package com.anthavio.aspect;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthavio.HibernateHelper;


/**
 * AspectJ aspect logs execution of any method marked with {@link Logged} annotation. 
 * When class is annotated, all public method executions are logged.
 * 
 * On INFO log level, only argument types (class) are logged (exception - null argument is logged every time)
 * On DEBUG log level, argument and return values (toString()) are logged
 * 
 * @author vanek
 */
@Aspect
@SuppressAjWarnings({ "adviceDidNotMatch" })
public class LogAspect {
	private static final String CONTINUE = "...";
	private static final String NULL = "null";
	private static final String ENTER = ">>";
	private static final String EXIT = "<<";
	private static final String ERROR = "<!";

	private static final boolean isHibernatePresent = HibernateHelper.isHibernatePresent();

	private Map<Signature, ExecStats> statsMap = new ConcurrentHashMap<Signature, ExecStats>();

	/** All public methods */
	//@Pointcut("within(com.anthavio..*) && execution(public * *(..))")
	@Pointcut("execution(public * *(..))")
	public final void publicMethod() {
	}

	/** public constructors	 */
	@Pointcut("execution(public new(..))")
	public final void publicConstructor() {
	}

	/** @Logged annotated classes */
	@Pointcut("within(@com.anthavio.aspect.Logged *)")
	public final void loggedClass() {
	}

	/** @Logged annotated methods */
	@Pointcut("execution(@com.anthavio.aspect.Logged * *(..))")
	public final void loggedMethod() {
	}

	/** Methods with @Logged */
	@Pointcut("loggedMethod() && @annotation(cfg)")
	public final void isLoggedMethod(Logged cfg) {
	}

	/** public method with @Logged annotated class */
	@Pointcut("publicMethod() && !loggedMethod() && loggedClass() && @target(cfg)")
	public final void isLoggedClassMethod(Logged cfg) {
	}

	/** public constructor of @Logged annotated class */
	@Around("publicConstructor() && loggedClass() && @target(cfg)")
	public void isLoggedClassConstructor(ProceedingJoinPoint pjp, Logged cfg) throws Throwable {
		around(pjp, cfg);
	}

	/** @Logged annotated constructor */
	@Around("execution(@com.anthavio.aspect.Logged new(..)) && @annotation(cfg)")
	public void isLoggedConstructor(ProceedingJoinPoint pjp, Logged cfg) throws Throwable {
		around(pjp, cfg);
	}

	@Around(value = "isLoggedClassMethod(cfg)", argNames = "cfg")
	public final Object classBasedLogAround(ProceedingJoinPoint pjp, Logged cfg) throws Throwable {
		return around(pjp, cfg);
	}

	@Around(value = "isLoggedMethod(cfg)", argNames = "cfg")
	public final Object around(ProceedingJoinPoint pjp, Logged cfg) throws Throwable {
		long startMillis = System.currentTimeMillis();
		Signature signature = pjp.getSignature();
		final Logger logger = getLogger(signature);

		//parameter values and return values are logged only on debug/trace level or when forced
		boolean logValues = logger.isDebugEnabled() || logger.isTraceEnabled() || cfg.forceValues();

		if (cfg.mode() == Logged.Mode.AROUND || cfg.mode() == Logged.Mode.ENTER) {
			print(buildEnterMessage(pjp, cfg, logValues), logger);
		}

		Object retVal = null;
		try {
			retVal = pjp.proceed();
			if (cfg.mode() == Logged.Mode.AROUND || cfg.mode() == Logged.Mode.EXIT) {
				long execMillis = System.currentTimeMillis() - startMillis;
				String message = buildExitMessage(signature, cfg, logValues, execMillis, retVal);
				print(message, logger);
				if (cfg.statistics()) {
					ExecStats stats = statsMap.get(signature);
					if (stats == null) {
						stats = new ExecStats();
						statsMap.put(signature, stats);
					}
					stats.execution(startMillis, execMillis);
				}
			}
		} catch (Exception x) {
			long execMillis = System.currentTimeMillis() - startMillis;
			printException(signature, cfg, logger, execMillis, x);
			if (cfg.statistics()) {
				ExecStats stats = statsMap.get(signature);
				if (stats == null) {
					stats = new ExecStats();
					statsMap.put(signature, stats);
				}
				stats.exception(startMillis, execMillis);
			}
			throw x;
		}
		return retVal;
	}

	private final Logger getLogger(final Signature signature) {
		String className = signature.getDeclaringType().getName();
		//String className = jp.getTarget().getClass().getName(); null target for static method
		final int idxCglib = className.indexOf("$$EnhancerByCGLIB$$");
		if (idxCglib != -1) {
			className = className.substring(0, idxCglib);
		}
		//We will not cache Loggers. Logback already does that
		return LoggerFactory.getLogger(className);
	}

	private final void print(final String message, final Logger logger) {
		if (logger.isTraceEnabled()) {
			logger.trace(message);
		} else if (logger.isDebugEnabled()) {
			logger.debug(message);
		} else if (logger.isInfoEnabled()) {
			logger.info(message);
		} else if (logger.isWarnEnabled()) {
			logger.warn(message);
		} else if (logger.isErrorEnabled()) {
			logger.error(message);
		} else {
			logger.warn("Unknown log level");
			logger.warn(message);
		}
	}

	private final String buildEnterMessage(final JoinPoint jp, final Logged log, boolean logValues) {
		final StringBuilder sb = new StringBuilder();
		sb.append(ENTER);
		sb.append(jp.getSignature().getName());
		sb.append('(');

		final Object[] args = jp.getArgs();

		for (int i = 0; i < args.length; ++i) {
			final Object arg = args[i];

			if (arg == null) {
				sb.append(NULL); //log null regardless other setting
			} else {

				boolean logVal = logValues;
				if (logValues) {
					final Class<?>[] exclTypes = log.notTypes();
					for (int j = 0; j < exclTypes.length; ++j) {
						if (exclTypes[j].isAssignableFrom(arg.getClass())) {
							logVal = false;
							break;
						}
					}

					final int[] exclParams = log.notParIdxs();
					for (int j = 0; j < exclParams.length; ++j) {
						if (i == exclParams[j]) {
							logVal = false;
							break;
						}
					}
				}

				if (logVal) {
					buildValue(arg, sb, log.maxLength());
				} else {
					sb.append(arg.getClass().getSimpleName());
				}
			}
			sb.append(',');
		}

		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(')');
		sb.append(ENTER);
		return sb.toString();
	}

	private final String buildExitMessage(final Signature signature, final Logged log, boolean logValue,
			final long execMillis, final Object retVal) {
		final StringBuilder sb = new StringBuilder();
		sb.append(EXIT);
		sb.append(signature.getName());

		boolean notVoid = (signature instanceof MethodSignature)
				&& ((MethodSignature) signature).getReturnType() != void.class;

		if (retVal == null && notVoid) {
			sb.append(": ");
			sb.append(NULL); //log null regardless other setting
		} else {

			if (log.logRetVal() && notVoid) {
				sb.append(": ");

				if (logValue) {
					final Class<?>[] exclTypes = log.notTypes();
					for (int j = 0; j < exclTypes.length; ++j) {
						if (exclTypes[j].isAssignableFrom(retVal.getClass())) {
							logValue = false;
							break;
						}
					}
					final int[] exclParams = log.notParIdxs();
					for (int j = 0; j < exclParams.length; ++j) {
						if (exclParams[j] == -1) { // index -1 means return value
							logValue = false;
							break;
						}
					}
				}

				if (logValue) {
					buildValue(retVal, sb, log.maxLength());
				} else {
					sb.append(retVal.getClass().getSimpleName());
				}
			}
		}
		sb.append(EXIT);

		if (log.logTime()) {
			sb.append(' ');
			sb.append(execMillis);
			sb.append("ms");
		}
		/*
		if (log.statistics()) {
			signature.toLongString();
			//System.out.println(signature.toLongString());
			//pjp.getSignature().;
			//execMillis
		}
		 */
		return sb.toString();
	}

	private final void printException(final Signature signature, final Logged cfg, final Logger logger,
			final long execMillis, final Exception x) {

		final StringBuilder sb = new StringBuilder();
		sb.append(ERROR);
		sb.append(signature.getName());
		sb.append(' ');
		sb.append(String.valueOf(x));
		sb.append(ERROR);

		if (cfg.logTime()) {
			sb.append(' ');
			sb.append(execMillis);
			sb.append("ms");
		}

		//ignore configured logger Level a use ERROR level
		if (cfg.stackTrace()) {
			logger.error(sb.toString(), x);
		} else {
			logger.error(sb.toString());
		}

	}

	private final void buildValue(Object value, StringBuilder sb, int max) {
		if (value == null) {
			sb.append(NULL);
		} else {
			if (isHibernatePresent) {
				value = HibernateHelper.getHibernateProxiedValue(value);
			}
			if (value instanceof Collection<?>) {
				int size = ((Collection<?>) value).size();
				sb.append(value.getClass().getSimpleName());
				sb.append("[").append(size).append("]");
			} else if (value instanceof Map<?, ?>) {
				int size = ((Map<?, ?>) value).size();
				sb.append(value.getClass().getSimpleName());
				sb.append("[").append(size).append("]");
			} else if (value.getClass().isArray()) {
				int size = Array.getLength(value);
				String simpleName = value.getClass().getSimpleName();
				sb.append(simpleName.substring(0, simpleName.length() - 2));
				sb.append("[").append(size).append("]");
			} else {
				String string = String.valueOf(value);
				if (string.length() > max) {
					sb.append(string.substring(0, max));
					sb.append(CONTINUE);
					sb.append(string.length());
				} else {
					sb.append(string);
				}
			}
		}
	}

	public static class ExecStats {

		public Date lastExecutionDate;

		public long lastExecutionTime;

		public Date lastExceptionDate;

		public long lastExceptionTime;

		public long average = 0;

		public int exceptions = 0;

		public long executions = 0;

		private void execution(long startMillis, long execMillis) {
			++executions;
			lastExecutionDate = new Date(startMillis);
			lastExecutionTime = execMillis;
			//average = (average * (executions - 1) + execMillis) / executions;
			average = average + ((execMillis - average) / executions);
		}

		private void exception(long startMillis, long execMillis) {
			execution(exceptions, average);
			++exceptions;
			lastExceptionDate = new Date(startMillis);
			lastExceptionTime = execMillis;
		}

	}
}