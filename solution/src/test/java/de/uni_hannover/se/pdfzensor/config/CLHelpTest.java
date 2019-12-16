package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.HelpArgumentProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.StringUtils;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link CLHelp}.
 */
class CLHelpTest {
	
	/** Basic tests related to {@link CLHelp}. */
	@Test
	void testGeneral() {
		TestUtility.assertIsUtilityClass(CLHelp.class);
	}
	
	/**
	 * This test checks if help or version is printed into System.out when it was requested
	 *
	 * @param args    the command-line arguments which were given from the ArgumentSource
	 * @param help    True if args contains -h
	 * @param version True if args contains -V
	 */
	@ParameterizedTest(name = "Run {index}: args: {0} => help: {1}, version: {2}")
	@ArgumentsSource(HelpArgumentProvider.class)
	void printStandardHelpOptionsIfRequested(String[] args, boolean help, boolean version) {
		final PrintStream originalOut = System.out;
		final var outContent = new ByteArrayOutputStream();
		try (final var printStream = new PrintStream(outContent)) {
			System.setOut(printStream);
			final boolean shouldPrint = help || version;
			assertEquals(shouldPrint, CLHelp.printStandardHelpOptionsIfRequested(args));
			assertEquals(shouldPrint, StringUtils.isNotBlank(outContent.toString()));
		} finally {
			System.setOut(originalOut);
		}
	}
	
	/**
	 * This test checks if the expected behavior is triggered on invalid inputs.
	 */
	@SuppressWarnings("ConstantConditions")
	@Test
	void invalidInput() {
		assertThrows(CommandLine.MissingParameterException.class, CLHelp::printStandardHelpOptionsIfRequested);
		assertThrows(NullPointerException.class, () -> CLHelp.printStandardHelpOptionsIfRequested((String[]) null));
		assertThrows(IllegalArgumentException.class, () -> CLHelp.printStandardHelpOptionsIfRequested((String) null));
	}
}