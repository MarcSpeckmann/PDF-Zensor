package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.config.Settings;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.CONFIG_PATH;
import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;

import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResourcePath;
import static org.junit.jupiter.api.Assertions.*;


public class PasswordUtilTest {

	@Test
	void testHandleIncorrectPasswordWithNoInteraction(){
		String[] args = {"-n", "file"};
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null, args)));

		args[0] = "--no-interaction";
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null , args)));
	}

	@Test
	void testGetPasswordFromCL(){
		String input = "pdfzensor is cool";
		System.setIn(new ByteArrayInputStream(input.getBytes()));
		assertEquals("pdfzensor is cool", PasswordUtil.getPasswordFromCL(new Settings(null, "null")));
	}

	@Test
	void testPasswordUtil(){
		System.setIn(new ByteArrayInputStream("oneRandomPassword".getBytes()));
		assertFalse(PasswordUtil.handleIncorrectPassword(new Settings(null, "file")));

		System.setIn(new ByteArrayInputStream("\n".getBytes()));
		assertTrue(PasswordUtil.handleIncorrectPassword(new Settings(null, "file")));
	}
}
