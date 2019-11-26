package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.TestUtility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link CLHelp}.
 */
class CLHelpTest {
	
	/**
	 * Creates arguments for a function call of {@link #printStandardHelpOptionsIfRequested(String[], File, boolean,
	 * boolean)}
	 */
	@NotNull
	private static Arguments createArgumentCLHelp(@NotNull String in, boolean help, boolean version) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (help) arguments.add("-h");
		if (version) arguments.add("-V");
		var inFile = new File(in).getAbsoluteFile();
		
		return Arguments.of(arguments.toArray(new String[0]), inFile, help, version);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #printStandardHelpOptionsIfRequested(String[],
	 * File, boolean, boolean)}.
	 */
	private static Stream<Arguments> testArgumentsCLHelp() {
		String inputFile = "src/test/resources/sample.pdf";
		boolean[] bools = {true, false};
		
		var list = new ArrayList<Arguments>();
		for (boolean help : bools)
			for (boolean version : bools)
				list.add(createArgumentCLHelp(inputFile, help, version));
		return list.stream();
	}
	
	/**
	 * Basic tests related to {@link CLHelp}
	 */
	@Test
	void testCLHelp() {
		TestUtility.assertIsUtilityClass(CLHelp.class);
	}
	
	/**
	 * Multiple tests on different inputs.
	 */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, help: {2}, version: {3}")
	@MethodSource("testArgumentsCLHelp")
	void printStandardHelpOptionsIfRequested(String[] args, File inFile, boolean help, boolean version) {
		final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		final PrintStream originalOut = System.out;
		System.setOut(new PrintStream(outContent));
		if (help || version) {
			assertDoesNotThrow(() -> CLHelp.printStandardHelpOptionsIfRequested(args));
			assertTrue(CLHelp.printStandardHelpOptionsIfRequested(args));
			assertNotNull(outContent.toString());
			// check if something was written into the output
			assertTrue(outContent.toString()
								 .length() > 10);
		} else {
			assertFalse(CLHelp.printStandardHelpOptionsIfRequested(args));
		}
		System.setOut(originalOut);
		
	}
}