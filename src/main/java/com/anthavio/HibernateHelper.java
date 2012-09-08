/**
 * 
 */
package com.anthavio;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vanek
 *
 */
public class HibernateHelper {

	private static final Logger log = LoggerFactory.getLogger(HibernateHelper.class);

	public static final String UNACCESSIBLE_VALUE = "???";

	private static Class<?> hibernateProxyClass;

	private static Method isInitializedMethod;

	private static Class<?> hibernateCollectionClass;

	private static Class<?> hibernateClass;

	private static Method getHibernateLazyInitializer;

	private static Method getIdentifier;

	static {
		try {
			/*
			 * org.hibernate.Hibernate
			 * 
			 * public static boolean isInitialized(Object proxy) { if (proxy
			 * instanceof HibernateProxy) { return
			 * (!(((HibernateProxy)proxy).getHibernateLazyInitializer
			 * ().isUninitialized())); } if (proxy instanceof PersistentCollection) {
			 * return ((PersistentCollection)proxy).wasInitialized(); } return true; }
			 */
			hibernateClass = Class.forName("org.hibernate.Hibernate");
			isInitializedMethod = hibernateClass.getMethod("isInitialized", Object.class);

			try {
				hibernateProxyClass = Class.forName("org.hibernate.proxy.HibernateProxy");
			} catch (ClassNotFoundException cnfx) {
				throw new IllegalStateException(
						"Found org.hibernate.Hibernate class but not org.hibernate.proxy.HibernateProxy");
			}

			try {
				//hibernate 3.x - swap lookup order when Hibernate 4.x became more common
				hibernateCollectionClass = Class.forName("org.hibernate.collection.PersistentCollection");
			} catch (ClassNotFoundException cnfx) {
				try {
					//hibernate 4.x - swap lookup order when Hibernate 3.x became less common
					hibernateCollectionClass = Class.forName("org.hibernate.collection.spi.PersistentCollection");
				} catch (ClassNotFoundException cnfx2) {
					throw new IllegalStateException(
							"Did not found Hibernate neither 3.x nor 4.x PersistentCollection class");
				}
			}

			getHibernateLazyInitializer = hibernateProxyClass.getMethod("getHibernateLazyInitializer", (Class<?>[]) null);

			Class<?> initializerClass = Class.forName("org.hibernate.proxy.LazyInitializer");
			getIdentifier = initializerClass.getMethod("getIdentifier", (Class<?>[]) null);

			// Class<?> collectionClass =
			// Class.forName("org.hibernate.collection.PersistentCollection");
			// wasInitialized = collectionClass.getMethod("wasInitialized",
			// (Class<?>[]) null);
			log.debug("Hibernate support enabled. Hibernate classes are present");
		} catch (ClassNotFoundException cnfx) {
			// Hibernate proste neni v classpath
			log.debug("Hibernate support disabled. Hibernate classes not present");
		} catch (SecurityException sx) {
			throw new IllegalStateException("Error detecting Hibernate",sx);
		} catch (NoSuchMethodException nsmx) {
			throw new IllegalStateException("Error detecting Hibernate",nsmx);
		}
	}

	public static boolean isHibernatePresent() {
		return (hibernateClass != null);
	}

	/**
	 * If @param value is HibernateProxy or PersistentCollection
	 * and it is not initialized (not prefetched or lazy loaded)
	 * then return only reference identifier
	 */
	public static Object getHibernateProxiedValue(Object value) {
		Class<? extends Object> valueClass = value.getClass();
		if (hibernateProxyClass.isAssignableFrom(valueClass)) {
			try {
				boolean initialized = (Boolean) isInitializedMethod.invoke(null, value);
				if (initialized == false) {
					Object lazyInitializer = getHibernateLazyInitializer.invoke(value, (Object[]) null);
					Object identifier = getIdentifier.invoke(lazyInitializer, (Object[]) null);
					return "<id:" + identifier + ">";
				}
			} catch (Exception x) {
				log.warn("Failed access hibernate proxy", x);
				return UNACCESSIBLE_VALUE + x;
			}
		} else if (hibernateCollectionClass.isAssignableFrom(valueClass)) {
			try {
				boolean initialized = (Boolean) isInitializedMethod.invoke(null, value);
				if (initialized == false) {
					return "<lazy>";
				}
			} catch (Exception x) {
				log.warn("Failed access hibernate collection wraper", x);
				return UNACCESSIBLE_VALUE + x;
			}
		}

		return value;
	}
}
