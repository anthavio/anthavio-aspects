package com.anthavio.aspect.test;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.anthavio.HibernateHelper;
import com.anthavio.aspect.Logged;
import com.anthavio.aspect.Logged.Mode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 
 * @author vanek
 * 
 * Spis kvuli vypisu do konzole a esteticke posouzeni logovaciho vypisu nez opravdovy test
 */
public class LogAspectTest {

	public LogAspectTest() {

	}

	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	@BeforeClass
	public void beforeClass() {
		HibernateHelper.isHibernatePresent(); //static class initializer prints to log
	}

	@BeforeMethod
	public void before() {
		lc.getLogger(LogAspectTest.class).setLevel(Level.DEBUG);
		EventStoringAppender.getEvents().clear();
	}

	/**
	 * V DEBUG levelu se musi logovat skutecne poslane hodnoty parametru
	 */
	@Test
	public void testSimpleDebug() {
		lc.getLogger(LogAspectTest.class).setLevel(Level.DEBUG);
		String arg1 = "argument1";
		simpleString(arg1);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getLevel().levelInt).isEqualTo(Level.DEBUG_INT);
		assertThat(enterEvent.getMessage()).contains(arg1);

		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getLevel().levelInt).isEqualTo(Level.DEBUG_INT);
		assertThat(exitEvent.getMessage()).contains(arg1);
	}

	/**
	 * V INFO levelu se musi logovat pouze jmena trid parametru
	 */
	@Test
	public void testSimpleInfo() {
		lc.getLogger(LogAspectTest.class).setLevel(Level.INFO);

		String arg1 = "argument1";
		simpleString(arg1);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getLevel().levelInt).isEqualTo(Level.INFO_INT);
		assertThat(enterEvent.getMessage()).contains("String");

		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getLevel().levelInt).isEqualTo(Level.INFO_INT);
		assertThat(exitEvent.getMessage()).contains("String");
	}

	/**
	 * V INFO levelu lze prinutit vypis hodnot
	 */
	@Test
	public void testSimpleInfoForced() {
		lc.getLogger(LogAspectTest.class).setLevel(Level.INFO);
		String arg1 = "argument1";
		simpleStringForced(arg1);

		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getLevel().levelInt).isEqualTo(Level.INFO_INT);
		assertThat(enterEvent.getMessage()).contains(arg1);

		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getLevel().levelInt).isEqualTo(Level.INFO_INT);
		assertThat(exitEvent.getMessage()).contains(arg1);
	}

	@Test
	public void testEnter() {
		simpleEnter(null, 666);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
	}

	@Test
	public void testExit() {
		simpleExit(new Date());
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(1);
	}

	@Test
	public void testNotTypeValues() {
		String arg1 = "argument1";
		notTypeValues(arg1);

		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getMessage()).doesNotContain(arg1);
		assertThat(enterEvent.getMessage()).contains("String");

		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getMessage()).doesNotContain(arg1);
		assertThat(exitEvent.getMessage()).contains("String");
	}

	@Test
	public void testNotParIdx0() {
		String arg1 = "argument1";
		notParIdx0(arg1);

		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		//parameter is only type info
		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getMessage()).doesNotContain(arg1);
		assertThat(enterEvent.getMessage()).contains("String");

		//return is detail
		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getMessage()).contains(arg1);
		assertThat(exitEvent.getMessage()).doesNotContain("String");

		notParIdx0(null);//just in case...
	}

	@Test
	public void testNotRetVal() {
		String arg1 = "argument1";
		notRetVal(arg1);

		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		//parameter is detail
		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getMessage()).contains(arg1);
		assertThat(enterEvent.getMessage()).doesNotContain("String");

		//return is only type info
		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getMessage()).doesNotContain(arg1);
		assertThat(exitEvent.getMessage()).contains("String");

		notRetVal(null); //just in case...
	}

	@Test
	public void testVoid() {
		voidMethod();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		//return is void
		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(1);
		assertThat(enterEvent.getMessage()).doesNotContain("null");
	}

	@Test
	public void testVoidStatic() {
		voidStaticMethod();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		//return is void
		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(1);
		assertThat(enterEvent.getMessage()).doesNotContain("null");
	}

	@Test
	public void testException() {
		try {
			exception("BlaBlaBla", GregorianCalendar.getInstance());
			Assert.fail("Prosla vyjimka?!?");
		} catch (IllegalArgumentException iax) {
			//ok
		}
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		assertThat(enterEvent.getLevel().levelInt).isEqualTo(Level.DEBUG_INT);
		assertThat(enterEvent.getMessage()).contains("String");
		assertThat(enterEvent.getMessage()).contains("GregorianCalendar");

		ILoggingEvent exitEvent = EventStoringAppender.getEvents().get(1);
		assertThat(exitEvent.getLevel().levelInt).isEqualTo(Level.ERROR_INT); //ERROR_INT !!!
		assertThat(exitEvent.getMessage()).contains("BlaBlaBla");
		assertThat(exitEvent.getMessage()).contains("IllegalArgumentException");
	}

	@Test
	public void testCalendar() {
		lc.getLogger(LogAspectTest.class).setLevel(Level.INFO);
		calendar(Calendar.getInstance());
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testArrayInt() {
		arrayInt(new int[3]);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testArrayInteger() {
		arrayInteger(new Integer[3]);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testDateList() {
		ArrayList<Date> list = new ArrayList<Date>();
		list.add(new Date());
		dateList(list);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testDateMap() {
		//lc.getLogger(LogAspectTest.class).setLevel(Level.INFO);
		Map<String, Date> map = new HashMap<String, Date>();
		map.put("fromDate", new Date());
		map.put("untilDate", new Date());
		dateMap(map);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testxAnnotatedClass() {
		DummyAnnotatedClass dummy = new DummyAnnotatedClass();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		dummy.dummy(11);
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(4);

		dummy.yummy(); //nesmi zalogovat nepublic metodu
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(4);

		ILoggingEvent enterEvent = EventStoringAppender.getEvents().get(0);
		//default level v logback.xml je trace
		assertThat(enterEvent.getLevel().levelInt).isEqualTo(Level.TRACE_INT);
	}

	@Test
	public void testxAnnotatedClassParam() {
		DummyAnnotatedClass dummy = new DummyAnnotatedClass("argument");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		dummy.yummy(); //nesmi zalogovat nepublic metodu
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);

		new DummyAnnotatedClass(0); //nesmi zalogovat nepublic konstruktor
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testAnnotatedConstructor() {
		new DummyAnnotatedConstructor();
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testAnnotatedConstructorParam() {
		new DummyAnnotatedConstructor("argument");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Test
	public void testStaticMethod() {
		staticMethod("Whatever");
		assertThat(EventStoringAppender.getEvents().size()).isEqualTo(2);
	}

	@Logged
	private String simpleString(String param1) {
		return param1;
	}

	@Logged(forceValues = true)
	private String simpleStringForced(String param1) {
		return param1;
	}

	@Logged(mode = Mode.ENTER)
	public String simpleEnter(String param1, Integer param2) {
		return param1;
	}

	@Logged(mode = Mode.EXIT)
	public Date simpleExit(Date param1) {
		return param1;
	}

	@Logged(notTypes = String.class)
	private String notTypeValues(String param1) {
		return param1;
	}

	@Logged(notParIdxs = 0)
	private String notParIdx0(String param1) {
		return param1;
	}

	@Logged(notParIdxs = -1)
	private String notRetVal(String param1) {
		return param1;
	}

	@Logged(notParIdxs = 0, notTypes = { Calendar.class })
	private String exception(String param1, Calendar param2) {
		throw new IllegalArgumentException(param1);
	}

	@Logged
	public Calendar calendar(Calendar calendar) {
		return calendar;
	}

	@Logged
	public int[] arrayInt(int[] intArrayParam) {
		return intArrayParam;
	}

	@Logged
	public Integer[] arrayInteger(Integer[] intArrayParam) {
		return intArrayParam;
	}

	@Logged
	public List<Date> dateList(List<Date> listParam) {
		return listParam;
	}

	@Logged
	public Map<String, Date> dateMap(Map<String, Date> mapParam) {
		return mapParam;
	}

	@Logged
	private static String staticMethod(String s) {
		return s;
	}

	@Logged
	private static void voidStaticMethod() {
	}

	@Logged
	private void voidMethod() {
	}

}

class DummyAnnotatedConstructor {

	@Logged
	public DummyAnnotatedConstructor() {

	}

	@Logged
	protected DummyAnnotatedConstructor(String string) {

	}

	public String dummy(int i) {
		return "Number " + i;
	}

	protected String yummy() {
		return "";
	}

	@Logged
	protected String gummy() {
		return "";
	}
}

@Logged
class DummyAnnotatedClass {

	public DummyAnnotatedClass() {

	}

	public DummyAnnotatedClass(String string) {

	}

	protected DummyAnnotatedClass(int i) {

	}

	public String dummy(int i) {
		return "Number " + i;
	}

	protected String yummy() {
		return "";
	}

	@Logged
	protected String gummy() {
		return "";
	}
}
