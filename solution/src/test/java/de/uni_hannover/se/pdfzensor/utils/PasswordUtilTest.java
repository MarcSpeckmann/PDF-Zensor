package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.config.Settings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;


/** PasswordUtilTest should contain all unit-tests related to {@link PasswordUtil}.*/
public class PasswordUtilTest {

	/**
	 * Tests if handleIncorrectPassword always returns true if no-interaction is set.
	 */
	@Test
	void testHandleIncorrectPasswordWithNoInteraction(){
		String[] args = {"-n", "file"};
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null, args)));

		args[0] = "--no-interaction";
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null , args)));
	}

	/**
	 * Tests if getPasswordFromCL returns the user input.
	 */
	@Test
	void testGetPasswordFromCL(){
		String input = "pdfzensor is cool";
		System.setIn(new ByteArrayInputStream(input.getBytes()));
		assertEquals("pdfzensor is cool", PasswordUtil.getPasswordFromCL(new Settings(null, "null")));
	}

	/**
	 * Tests if PasswordUtil handles the user input in the right way.
	 */
	@Test
	void testPasswordUtil(){
		System.setIn(new ByteArrayInputStream("oneRandomPassword".getBytes()));
		assertFalse(PasswordUtil.handleIncorrectPassword(new Settings(null, "file")));

		System.setIn(new ByteArrayInputStream("\n".getBytes()));
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null, "file")));
	}
}
