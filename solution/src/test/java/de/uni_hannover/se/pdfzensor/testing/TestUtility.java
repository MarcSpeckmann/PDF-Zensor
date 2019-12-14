package de.uni_hannover.se.pdfzensor.testing;

import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The TestUtility class provides some useful functionality that may be reused in multiple tests. It's main purpose is
 * to outsource some functionality to keep the tests comprehensible yet comprehensive.
 */
public final class TestUtility {
	
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
	 * Compares the bounds of two rectangles with consideration to a small error margin.
	 *
	 * @param expected The expected rectangle bounds.
	 * @param actual   The actual rectangle bounds.
	 * @return True if the bounds of the rectangles are equal according to the margin, false otherwise.
	 */
	public static boolean checkRectanglesEqual(@NotNull Rectangle2D expected, @NotNull Rectangle2D actual) {
		var range = 1 / 1000000.0;
		Objects.requireNonNull(expected);
		Objects.requireNonNull(actual);
		return (range > Math.abs(expected.getX() - actual.getX())) &&
			   (range > Math.abs(expected.getY() - actual.getY())) &&
			   (range > Math.abs(expected.getWidth() - actual.getWidth())) &&
			   (range > Math.abs(expected.getHeight() - actual.getHeight()));
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
	
	public static Method getPrivateMethod(@NotNull Class<?> cls, @NotNull String methodName, Class<?>... paramTypes) {
		try {
			var method = cls.getDeclaredMethod(methodName, paramTypes);
			method.setAccessible(true);
			return method;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * retrieve a private field from a given instance
	 * @param aClass the class that we get the private field from
	 * @param fieldName the name of the field that should be return
	 * @param instance the instance of the class
	 * @param dummyField an dummy instance of the wanted field so it can be casted (DONT USE VAR AS INPUT)
	 * @param <T>  the content type of class.
	 * @param <K>  the content type of Field.
	 * @return the Wanted Private Field
	 */
	public static <T, K> K getPrivateParameter(@NotNull Class<?> aClass, @NotNull String fieldName, @NotNull T instance, @Nullable K dummyField) {
		try {
			var parameter = aClass.getDeclaredField(fieldName);
			parameter.setAccessible(true);
			return (K) parameter.get(instance);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw  new RuntimeException("Could not retrieve the " + fieldName ,e);
		}
	}
}
