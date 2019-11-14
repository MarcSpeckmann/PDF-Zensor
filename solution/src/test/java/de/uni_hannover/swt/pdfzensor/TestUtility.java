package de.uni_hannover.swt.pdfzensor;

import com.fasterxml.jackson.databind.util.ClassUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public final class TestUtility {

	public static <T> void assertIsUtilityClass(Class<T> cls) {
		assertNotNull(cls);
		assertTrue(Modifier.isFinal(cls.getModifiers()));
		for (var c : cls.getDeclaredConstructors())
			assertEquals(0, c.getParameterCount());
		try {
			var c = cls.getDeclaredConstructor();
			assertTrue(Modifier.isPrivate(c.getModifiers()));
			assertThrows(IllegalAccessException.class, c::newInstance);
			c.setAccessible(true);
			assertThrows(InvocationTargetException.class, c::newInstance);
		} catch (NoSuchMethodException e) {
			fail("A utility-class should contain a private default-constructor that throws an Exception.");
		}
		for (var m : cls.getDeclaredMethods())
			assertTrue(Modifier.isStatic(m.getModifiers()));
		for (var f : cls.getDeclaredFields())
			assertTrue(Modifier.isStatic(f.getModifiers()));
	}

}
