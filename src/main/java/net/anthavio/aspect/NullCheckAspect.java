package net.anthavio.aspect;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 
 * @author vanek
 *
 * AspectJ aspect checking null method arguments.
 * Uses {@link NullCheck} marking annotation
 */
@Aspect
@SuppressAjWarnings({ "adviceDidNotMatch" })
public class NullCheckAspect {

	//@Pointcut("execution(* *.*(@net.anthavio.aspect.NullCheck (*),..)) && @annotation(check)")

	/**
	 * Metods having some prarameter annotated with @NullCheck
	 */
	@Pointcut("execution(* *.*(.., @net.anthavio.aspect.NullCheck (*),..))")
	public void isNullCheckParam() {
	}

	/**
	 * Constructors having some prarameter annotated with @NullCheck
	 */
	@Pointcut("execution(*.new(.., @net.anthavio.aspect.NullCheck (*),..))")
	public void isNullCheckConstructorParam() {
	}

	//@Pointcut("execution((@net.anthavio.aspect.NullCheck *) *(..))")
	/**
	 * Methods having @NullCheck annotation but not void - check return value for nullness
	 */
	@Pointcut("@annotation(net.anthavio.aspect.NullCheck) && execution(!void *(..))")
	public void isNullCheckRetVal() {
	}

	/**
	 * Constructors having @NullCheck annotation - check all parameters for nullness
	 */
	@Pointcut("@annotation(net.anthavio.aspect.NullCheck) && execution(*.new(..))")
	public void isNullCheckConstructor() {
	}

	//@SuppressAjWarnings("adviceDidNotMatch")
	@AfterReturning(value = "isNullCheckRetVal()", returning = "retVal")
	public void checkReturnValue(JoinPoint joinPoint, Object retVal) {
		if (retVal == null) {
			throw new IllegalArgumentException("Null return value of method " + joinPoint.getSignature().toString());
		}
	}

	//@SuppressAjWarnings("adviceDidNotMatch")
	@Before(value = "isNullCheckParam()")
	public void checkMethodArguments(JoinPoint joinPoint) {

		// the method that is being called
		final CacheEntry info = getTargetCodeInfo(joinPoint.getSignature());

		// an array of arguments of the called method
		final Object[] arguments = joinPoint.getArgs();

		// get all annotations defined on method's parameters
		Annotation[][] parameterAnnotations = info.parameterAnnotations;
		int parameterIndex = 0;
		for (final Annotation[] annotations : parameterAnnotations) {
			for (final Annotation annotation : annotations) {
				if (annotation instanceof NullCheck) {
					Object argument = arguments[parameterIndex];
					if (argument == null) {
						Class<?> argumentType = info.parameterTypes[parameterIndex];
						throw new IllegalArgumentException("Null " + argumentType.getSimpleName() + " argument on position "
								+ (parameterIndex + 1) + " of method " + joinPoint.getSignature().toString());
					}
					break;
				}
			}
			parameterIndex++;
		}
	}

	//@SuppressAjWarnings("adviceDidNotMatch")
	@Before(value = "isNullCheckConstructor()")
	public void checkAllConstructorArguments(JoinPoint joinPoint) {
		final CacheEntry info = getTargetCodeInfo(joinPoint.getSignature());

		final Object[] arguments = joinPoint.getArgs();
		for (int parameterIndex = 0; parameterIndex < arguments.length; ++parameterIndex) {
			if (arguments[parameterIndex] == null) {
				Class<?> argumentType = info.parameterTypes[parameterIndex];
				throw new IllegalArgumentException("Null " + argumentType.getSimpleName() + " argument on position "
						+ (parameterIndex + 1) + " of constructor " + joinPoint.getSignature().toString());
			}
		}

	}

	//@SuppressAjWarnings("adviceDidNotMatch")
	@Before(value = "isNullCheckConstructorParam()")
	public void checkConstructorArguments(JoinPoint joinPoint) {

		// the constructor that is being called
		final CacheEntry info = getTargetCodeInfo(joinPoint.getSignature());

		// an array of arguments of the called method
		final Object[] arguments = joinPoint.getArgs();

		// get all annotations defined on method's parameters
		Annotation[][] parameterAnnotations = info.parameterAnnotations;
		int parameterIndex = 0;
		for (final Annotation[] annotations : parameterAnnotations) {
			for (final Annotation annotation : annotations) {
				if (annotation instanceof NullCheck) {
					Object argument = arguments[parameterIndex];
					if (argument == null) {
						Class<?> argumentType = info.parameterTypes[parameterIndex];
						throw new IllegalArgumentException("Null " + argumentType.getSimpleName() + " argument on position "
								+ (parameterIndex + 1) + " of constructor " + joinPoint.getSignature().toString());
					}
					break;
				}
			}
			parameterIndex++;
		}
	}

	//Not thread safe, worst case is just to be executed multiple times for some JoinPoint, which is little nuisance
	//Hmmmm what about WeakHashMap as store.... 
	private final Map<String, SoftReference<CacheEntry>> cache = new HashMap<String, SoftReference<CacheEntry>>();

	private static class CacheEntry {

		final Annotation[][] parameterAnnotations;

		final Class<?>[] parameterTypes;

		public CacheEntry(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
			this.parameterAnnotations = parameterAnnotations;
			this.parameterTypes = parameterTypes;
		}

	}

	private CacheEntry getTargetCodeInfo(Signature signature) {

		final String longSignature = signature.toLongString(); //AspectJ caches signature Strings
		SoftReference<CacheEntry> reference = cache.get(longSignature);
		CacheEntry retval;
		if (reference == null) { //Signature first contact
			retval = buildCacheEntry(signature, longSignature);
			reference = new SoftReference<CacheEntry>(retval);
			cache.put(longSignature, reference);
		} else {
			retval = reference.get();
			if (retval == null) { //SoftReference value GC'ed
				retval = buildCacheEntry(signature, longSignature);
				reference = new SoftReference<CacheEntry>(retval);
				cache.put(longSignature, reference);
			}
		}
		return retval;
	}

	private CacheEntry buildCacheEntry(Signature signature, final String longSignature) {
		CacheEntry retval;

		final Class<?> declaringType = signature.getDeclaringType();
		final Class<?>[] paramClasses = ((CodeSignature) signature).getParameterTypes();

		try {
			if (signature instanceof MethodSignature) {
				Method method = declaringType.getDeclaredMethod(signature.getName(), paramClasses);
				retval = new CacheEntry(method.getParameterAnnotations(), paramClasses);

			} else if (signature instanceof ConstructorSignature) {
				Constructor<?> constructor = declaringType.getDeclaredConstructor(paramClasses);
				retval = new CacheEntry(constructor.getParameterAnnotations(), paramClasses);

			} else {
				throw new UnsupportedOperationException("Unsupported signature type " + longSignature);
			}

		} catch (NoSuchMethodException x) {
			//Throw UnsupportedOperationException is little desperate...
			throw new UnsupportedOperationException("Method/Constructor not found by signature " + signature.toLongString(),
					x);
		}
		return retval;
	}

	/**
	 * @param signature
	 * @return Volanou metodu
	 
	private CacheEntry getTargetMethod(Signature signature) {
		String longSignature = signature.toLongString(); //AspectJ caches signature Strings
		CacheEntry retval = cache.get(longSignature);
		if (retval == null) {
			//jaka trida?
			Class<?> declaringType = signature.getDeclaringType();
			//jake parametry?
			Class<?>[] parcls = prepareParamsClasses(longSignature);
			try {
				Method method = declaringType.getDeclaredMethod(signature.getName(), parcls);
				retval = new CacheEntry(method.getParameterAnnotations(), parcls);
			} catch (Exception x) {
				//Vyhazovat UnsupportedOperationException je trochu zoufalost...
				throw new UnsupportedOperationException("Impossible happend", x);
			}
			cache.put(longSignature, retval);
		}

		return retval;
	}
	*/
	/**
	 * @param signature
	 * @return Volany konstruktor

	private CacheEntry getTargetConstructor(Signature signature) {
		String longSignature = signature.toLongString();
		CacheEntry retval = constructorCache.get(longSignature);
		if (retval == null) {
			//jaka trida?
			Class<?> declaringType = signature.getDeclaringType();
			//jake parametry?
			Class<?>[] parcls = prepareParamsClasses(longSignature);
			try {
				Constructor<?> constructor = declaringType.getDeclaredConstructor(parcls);
				retval = new CacheEntry(constructor.getParameterAnnotations(), parcls);
			} catch (Exception x) {
				//Vyhazovat UnsupportedOperationException je trochu zoufalost...
				throw new UnsupportedOperationException("Impossible happend", x);
			}
			constructorCache.put(longSignature, retval);
		}

		return retval;
	}
	*/

	/**
	 * @param longSignature
	 * @return
	 
	private Class<?>[] prepareParamsClasses(String longSignature) {
		String paramsPart = longSignature.substring(longSignature.indexOf('(') + 1,
				longSignature.length() - 1);
		String[] params = paramsPart.split(", ");
		Class<?>[] parcls = new Class[params.length];
		try {
			for (int i = 0; i < params.length; ++i) {
				//primitives
				String param = params[i];
				if (param.equals("boolean")) {
					parcls[i] = Boolean.TYPE;
				} else if (param.equals("byte")) {
					parcls[i] = Byte.TYPE;
				} else if (param.equals("char")) {
					parcls[i] = Character.TYPE;
				} else if (param.equals("short")) {
					parcls[i] = Short.TYPE;
				} else if (param.equals("int")) {
					parcls[i] = Integer.TYPE;
				} else if (param.equals("long")) {
					parcls[i] = Long.TYPE;
				} else if (param.equals("float")) {
					parcls[i] = Float.TYPE;
				} else if (param.equals("double")) {
					parcls[i] = Double.TYPE;
				} else if (param.indexOf("[") != -1) {
					//arrays
					int lparIdx = param.indexOf("[");
					String base = "[";
					for (int j = lparIdx + 1; j < param.length() - 1; ++j) {
						if (param.charAt(j) == '[') {
							base += "[";
						}
					}
					if (param.startsWith("boolean")) {
						base += "Z";
					} else if (param.startsWith("byte")) {
						base += "B";
					} else if (param.startsWith("char")) {
						base += "C";
					} else if (param.startsWith("short")) {
						base += "S";
					} else if (param.startsWith("int")) {
						base += "I";
					} else if (param.startsWith("long")) {
						base += "J";
					} else if (param.startsWith("float")) {
						base += "F";
					} else if (param.startsWith("double")) {
						base += "D";
					} else {
						base += "L" + param.substring(0, lparIdx) + ";";
					}
					parcls[i] = Class.forName(base);
				} else {
					parcls[i] = Class.forName(param);
				}
			}
		} catch (ClassNotFoundException cnfx) {
			throw new TypeNotPresentException("Impossible happend", cnfx);
		}
		return parcls;
	}
	*/

}
