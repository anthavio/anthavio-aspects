package net.anthavio.aspect.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anthavio.aspect.NullCheck;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * @author martin.vanek
 *
 */
public class NullCheckAspectTest {

	@Test
	public void testParamSecond() {
		try {
			doParamSecond(1, null);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}

	}

	@Test
	public void testParamFirst() {
		try {
			doParamFirst(null, 11);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testParamTypes() {
		doParamsArrayAndPrimitive(new Object(), true, (short) 1, 1, 1, 1.1f, 1.1d, new Object[0], null, null, null, null,
				null, null, new boolean[0][0], new String[0][0][0][0][0]);
		try {
			doParamsArrayAndPrimitive(null, true, (short) 1, 1, 1, 1.1f, 1.1d, null, null, null, null, null, null, null,
					null, null);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testParamTypesGeneric() {
		doParamsGenerics(new ArrayList<String>(), new HashMap<String, Long>());
		try {
			doParamsGenerics(null, null);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testStatic() {
		try {
			doStatic(null);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}

	}

	@Test
	public void testRetVal() {
		try {
			doRetVal();
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testRetVal2() {
		try {
			doRetVal2(1, 2);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testConstructor() {

		new DummyConstructor("xyz");

		try {
			new DummyConstructor(null);
			Assertions.fail("On no! Null passed the check! u konstruktoru");
		} catch (IllegalArgumentException iax) {
			//ok
		}

		new DummyConstructor(1, 2);

		try {
			new DummyConstructor(1, null);
			Assertions.fail("On no! Null passed the check! u konstruktoru");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testAll() {
		doAll(1); //works

		try {
			doAll(null);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}

		try {
			doAll(666);
			Assertions.fail("On no! Null passed the check!");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	private void doParamFirst(@NullCheck Integer s, int second) {

	}

	protected void doParamSecond(int first, @NullCheck String s) {

	}

	protected void doParamsArrayAndPrimitive(@NullCheck Object o, boolean b, short s, int i, long l, float f, double d,
			@NullCheck Object[] ao, boolean[] ba, short[] sa, int[] ia, long[] la, float[] fa, double[] da,
			@NullCheck boolean[][] ba2, @NullCheck String[][][][][] sa5) {
	}

	protected void doParamsGenerics(@NullCheck List<String> list, @NullCheck Map<String, Long> map) {

	}

	private static final void doStatic(@NullCheck Object s) {

	}

	@NullCheck
	void doRetValVoid(Integer s, Integer second) {
		//void returning method must not be advised
		//Don't know how to test it, but AJDT in Eclipse must not display arrow indicating joint point
	}

	@NullCheck
	private Integer doRetVal() {
		return null;
	}

	private @NullCheck Integer doRetVal2(Integer s, Integer second) {
		return null;
	}

	@NullCheck
	public Integer doAll(@NullCheck Integer x) {
		if (x == 666) {
			return null;
		} else {
			return x;
		}
	}
}

class DummyConstructor {

	public DummyConstructor(@NullCheck String yummy) {
	}

	@NullCheck
	public DummyConstructor(Integer xxx, Integer yyy) {

	}
}
