package com.anthavio.aspect.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.anthavio.aspect.NullCheck;


public class NullCheckAspectTest {

	@Test
	public void testParamDruhy() {
		try {
			doParamDruhy(1, null);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}

	}

	@Test
	public void testParamPrvni() {
		try {
			doParamPrvni(null, 11);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testParamTypes() {
		doParamsArrayAndPrimitive(new Object(), true, (short) 1, 1, 1, 1.1f, 1.1d, new Object[0], null,
				null, null, null, null, null, new boolean[0][0], new String[0][0][0][0][0]);
		try {
			doParamsArrayAndPrimitive(null, true, (short) 1, 1, 1, 1.1f, 1.1d, null, null, null, null,
					null, null, null, null, null);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testParamTypesGeneric() {
		doParamsGenerics(new ArrayList<String>(), new HashMap<String, Long>());
		try {
			doParamsGenerics(null, null);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testStatic() {
		try {
			doStatic(null);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}

	}

	@Test
	public void testRetVal() {
		try {
			doRetVal();
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testRetVal2() {
		try {
			doRetVal2(1, 2);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testConstructor() {

		new DummyConstructor("xyz");

		try {
			new DummyConstructor(null);
			Assert.fail("Prosla kontrola na null u konstruktoru");
		} catch (IllegalArgumentException iax) {
			//ok
		}

		new DummyConstructor(1, 2);

		try {
			new DummyConstructor(1, null);
			Assert.fail("Prosla kontrola na null u konstruktoru");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	@Test
	public void testAll() {
		doAll(1); //projde

		try {
			doAll(null);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}

		try {
			doAll(666);
			Assert.fail("Prosla kontrola na null");
		} catch (IllegalArgumentException iax) {
			//ok
		}
	}

	private void doParamPrvni(@NullCheck Integer s, int druhy) {

	}

	protected void doParamDruhy(int prvni, @NullCheck String s) {

	}

	protected void doParamsArrayAndPrimitive(@NullCheck Object o, boolean b, short s, int i, long l,
			float f, double d, @NullCheck Object[] ao, boolean[] ba, short[] sa, int[] ia, long[] la,
			float[] fa, double[] da, @NullCheck boolean[][] ba2, @NullCheck String[][][][][] sa5) {
	}

	protected void doParamsGenerics(@NullCheck List<String> list, @NullCheck Map<String, Long> map) {

	}

	private static final void doStatic(@NullCheck Object s) {

	}

	@NullCheck
	void doRetValVoid(Integer s, Integer druhy) {
		//void return nesmi byt advised
		//nevim jak to otestovat tak jen AJDT v Eclipse tu nesmi ukazat advised sipku
	}

	@NullCheck
	private Integer doRetVal() {
		return null;
	}

	private @NullCheck
	Integer doRetVal2(Integer s, Integer druhy) {
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

	public DummyConstructor(@NullCheck String hummy) {
	}

	@NullCheck
	public DummyConstructor(Integer xxx, Integer yyy) {

	}
}
