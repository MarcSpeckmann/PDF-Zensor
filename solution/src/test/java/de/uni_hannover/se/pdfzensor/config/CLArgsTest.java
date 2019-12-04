package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/** CLArgsTest should contain all unit-tests related to {@link CLArgs}. */
class CLArgsTest {
	/** Multiple tests related to using fromStringArray. */
	@SuppressWarnings("ConstantConditions")
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray((String) null));
		assertThrows(NullPointerException.class, () -> CLArgs.fromStringArray((String[]) null));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(new String[0]));
		assertThrows(IllegalArgumentException.class, CLArgs::fromStringArray);
	}
	
	/** Checks if the arguments are parsed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}, mode: {4}")
	@ArgumentsSource(CLArgumentProvider.class)
	void testArgsParser(String[] args, File input, File output, Level verbosity, Mode mode) {
		var clArgs = CLArgs.fromStringArray(args);
		assertEquals(input, clArgs.getInput());
		assertEquals(output, clArgs.getOutput());
		assertEquals(verbosity, clArgs.getVerbosity());
		assertEquals(mode, clArgs.getMode());
	}
}