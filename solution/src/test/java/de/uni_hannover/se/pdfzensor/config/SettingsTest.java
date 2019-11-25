package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	
	/**
	 * Creates arguments for a function call of {@link #testSettingsNoConfig(String[], File, File)} from the provided
	 * data.
	 */
	@NotNull
	private static Arguments createArgument(@NotNull String in, @NotNull String out) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		arguments.add("-o");
		arguments.add(out);
		var inFile = new File(in).getAbsoluteFile();
		var outFile = new File(out).getAbsoluteFile();
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #testSettingsNoConfig(String[], File,
	 * File)}.
	 */
	private static Stream<Arguments> testArguments() {
		String[] inputFiles = {getResource("/pdf-files/sample.pdf").getAbsolutePath(), getResource(
				"/pdf-files/sample.bla.pdf").getAbsolutePath()};
		String[] outputFiles = {"file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				list.add(createArgument(in, out));
		return list.stream();
	}
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@SuppressWarnings("ConstantConditions")
	@Test
	void testSettings() {
		// if the command line argument is not given or has a faulty structure
		assertThrows(NullPointerException.class, () -> new Settings((String[]) null));
		assertThrows(IllegalArgumentException.class, () -> new Settings((String) null));
		// if the command line argument is given but invalid
		assertThrows(IllegalArgumentException.class, () -> new Settings("this_file_does_not_exist.pdf"));
	}
	
	/** Checks if the arguments are passed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}")
	@MethodSource("testArguments")
	void testSettingsNoConfig(String[] args, File input, File output) throws IOException {
		final var settings = new Settings(args);
		assertEquals(input, settings.getInput());
		assertEquals(output, settings.getOutput());
	}
}