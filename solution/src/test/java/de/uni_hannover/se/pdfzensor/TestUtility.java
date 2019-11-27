package de.uni_hannover.se.pdfzensor;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

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
	
	@NotNull
	@SuppressWarnings("unchecked")
	public static Optional<org.apache.logging.log4j.core.Logger> getRootLogger() {
		try {
			var method = Logging.class.getDeclaredMethod("getRootLogger");
			method.setAccessible(true);
			var logger = method.invoke(Logger.class);
			return (Optional<org.apache.logging.log4j.core.Logger>) logger;
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the RootLogger.", e);
		}
		return Optional.empty();
	}
}
