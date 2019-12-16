package de.uni_hannover.se.pdfzensor.testing;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.function.Executable;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The TestUtility class provides some useful functionality that may be reused in multiple tests. It's main purpose is
 * to outsource some functionality to keep the tests comprehensible yet comprehensive.
 */
public final class TestUtility {
	public static final double EPSILON = 1e-6;
	
	/**
	 * Asserts that the provided class is not null and a static utility class. A static utility class should contain
	 * only static members (hence the name) and thus should be final (overriding does not make much sense). Furthermore
	 * it should have a private constructor such that it may not be instantiated on accident that throws an exception
	 * when called (possibly through reflection).
	 *
	 * @param cls the class to test
	 */
	public static void assertIsUtilityClass(@NotNull Class<?> cls) {
		assertNotNull(cls);
		assertTrue(Modifier.isFinal(cls.getModifiers()), "A utility class should be final");
		for (var c : cls.getDeclaredConstructors())
			assertEquals(0, c.getParameterCount(),
						 String.format("%s may not contain any constructor other than the default", cls.getName()));
		try {
			var c = cls.getDeclaredConstructor();
			assertTrue(Modifier.isPrivate(c.getModifiers()), "The default constructor should be private");
			assertThrows(IllegalAccessException.class, c::newInstance);
			c.setAccessible(true);
			assertThrows(InvocationTargetException.class, c::newInstance,
						 "The default constructor should throw an exception");
			c.setAccessible(false);
		} catch (NoSuchMethodException e) {
			fail("A utility-class should contain a private default-constructor that throws an Exception.");
		}
		for (var m : cls.getDeclaredMethods())
			assertTrue(Modifier.isStatic(m.getModifiers()),
					   String.format("%s::%s is not static", cls.getName(), m.getName()));
		for (var f : cls.getDeclaredFields())
			assertTrue(Modifier.isStatic(f.getModifiers()),
					   String.format("%s::%s is not static", cls.getName(), f.getName()));
	}
	
	/**
	 * Retrieves the given resource from the provided location. This will use the caller-class' {@link
	 * Class#getResource(String)}.
	 *
	 * @param path the path in the resources to load the file from. Should not be null and should start with a slash.
	 * @return the file that is located at the given resource-path.
	 */
	@NotNull
	@Contract("_ -> new")
	public static File getResource(@NotNull String path) {
		Objects.requireNonNull(path);
		var caller = StackLocatorUtil.getCallerClass(2);
		return new File(URLDecoder.decode(caller.getResource(path).getFile(), StandardCharsets.UTF_8));
	}
	
	/**
	 * Retrieves the Absolute Path of the given resource from the provided location. This will use the caller-class'
	 * {@link Class#getResource(String)}.
	 *
	 * @param path the path in the resources to get the Absolute Path from. Should not be null and should start with a
	 *             slash.
	 * @return the Absolute Path of the given resource-path.
	 */
	@NotNull
	@Contract("_ -> new")
	public static String getResourcePath(@NotNull String path) {
		Objects.requireNonNull(path);
		var caller = StackLocatorUtil.getCallerClass(2);
		return URLDecoder.decode(caller.getResource(path).getFile(), StandardCharsets.UTF_8);
	}
	
	/**
	 * Checks if the two floating-point numbers are approximately equal. Returns true if <code>{@code
	 * |d1-d2|<Ɛ}</code>.
	 *
	 * @param d1      the first number to be compared.
	 * @param d2      the second number to be compared.
	 * @param epsilon the precision of the comparison.
	 * @return true if <code>{@code |d1-d2|<Ɛ}</code>.
	 * @see #EPSILON
	 */
	public static boolean approx(double d1, double d2, double epsilon) {
		return abs(d1 - d2) < epsilon;
	}
	
	/**
	 * Compares the bounds of two rectangles with consideration to a small error margin.
	 *
	 * @param expected The expected rectangle bounds.
	 * @param actual   The actual rectangle bounds.
	 * @param epsilon  the precision of the comparison.
	 * @return True if the bounds of the rectangles are equal according to the margin, false otherwise.
	 */
	public static boolean checkRectanglesEqual(@NotNull Rectangle2D expected, @NotNull Rectangle2D actual,
											   double epsilon) {
		Objects.requireNonNull(expected);
		Objects.requireNonNull(actual);
		return approx(expected.getX(), actual.getX(), epsilon) &&
			   approx(expected.getY(), actual.getY(), epsilon) &&
			   approx(expected.getWidth(), actual.getWidth(), epsilon) &&
			   approx(expected.getHeight(), actual.getHeight(), epsilon);
	}
	
	/**
	 * Performs a join on the two streams. That means that for each value-combinations of the both streams the joiner is
	 * called. The results are given in the resulting stream.
	 *
	 * @param s1     the stream with the first values of the join.
	 * @param s2     a collection of values the first stream is joined with.
	 * @param joiner a function that maps combinations of the values of s1 and s2 into one result instance.
	 * @param <T>    the content type of the first stream.
	 * @param <K>    the content type of the second value collection.
	 * @param <R>    the return-type.
	 * @return a stream consisting of the mapped combinations of s1's and s2's values.
	 */
	public static <T, K, R> Stream<R> crossJoin(@NotNull Stream<T> s1,
												@NotNull Collection<K> s2,
												@NotNull BiFunction<T, K, R> joiner) {
		Objects.requireNonNull(s1);
		Objects.requireNonNull(s2);
		Objects.requireNonNull(joiner);
		return s1.flatMap(t -> s2.stream().map(k -> joiner.apply(t, k)));
	}
	
	/**
	 * Returns the private method of the given class that has the provided name and parameter-types. Throws a {@link
	 * RuntimeException} if an error occurs.
	 *
	 * @param cls        the class of which to retrieve the private method. Not null.
	 * @param methodName the name of the method that should be retrieved. Not null.
	 * @param paramTypes the types of the parameters. Not null.
	 * @return the accessible {@link Method}-object that represents the desired method.
	 * @throws RuntimeException if the method could not be retrieved.
	 */
	@NotNull
	public static Method getPrivateMethod(@NotNull Class<?> cls, @NotNull String methodName, Class<?>... paramTypes) {
		try {
			var method = cls.getDeclaredMethod(methodName, Validate.noNullElements(paramTypes));
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the private field of the given class that has the provided name. Throws a {@link RuntimeException} if an
	 * error occurs.
	 *
	 * @param cls       the class of which to retrieve the private field. Not null.
	 * @param fieldName the name of the field that should be retrieved. Not null.
	 * @param <T>       the type of the field which will be retrieved.
	 * @return The field with the given name and the type T.
	 * @throws RuntimeException if the field could not be retrieved or cast to the type.
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public static <T, K> T getPrivateField(@NotNull Class<K> cls, K parameter, @NotNull String fieldName) {
		try {
			var field = cls.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T)field.get(parameter);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Asserts that the provided executable calls {@link System#exit(int)} with the expected error-code.
	 *
	 * @param code       the expected exit-code.
	 * @param executable the executable to test for a call to {@link System#exit(int)} with the desired exit code.
	 */
	public static void assertExitCode(int code, Executable executable) {
		var defaultSecManager = System.getSecurityManager();
		try {
			var manager = new SecurityManager() {
				int actualCode;
				
				@Override
				public void checkPermission(Permission perm) { /* allow anything. */ }
				
				@Override
				public void checkPermission(Permission perm, Object context) { /* allow anything. */ }
				
				@Override
				public void checkExit(final int status) {
					super.checkExit(status);
					actualCode = status;
					throw new SecurityException("Aborting System.exit()");
				}
			};
			System.setSecurityManager(manager);
			assertThrows(SecurityException.class, executable, "System.exit was not called");
			assertEquals(code, manager.actualCode, "Wrong exit code");
		} finally {
			System.setSecurityManager(defaultSecManager);
		}
	}
}
