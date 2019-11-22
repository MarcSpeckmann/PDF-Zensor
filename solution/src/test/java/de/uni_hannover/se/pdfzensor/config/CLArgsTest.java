package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

/** CLArgsTest should contain all unit-tests related to {@link CLArgs}. */
class CLArgsTest {
	
	/**
	 * Crates arguments for a function call of {@link #testArgsParser(String[], File, File, Level)} from the provided
	 * data.
	 */
	private static Arguments createArgument(@NotNull String in, @Nullable String out, int lvl) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		if (lvl > 0)
			arguments.add("-" + "v".repeat(lvl));
		
		var inFile = new File(in).getAbsoluteFile();
		var outFile = Optional.ofNullable(out)
							  .map(File::new)
							  .map(File::getAbsoluteFile)
							  .orElse(null);
		Level verbosity = null;
		if (lvl > 0 && lvl < VERBOSITY_LEVELS.length) verbosity = VERBOSITY_LEVELS[lvl];
		else if (lvl >= VERBOSITY_LEVELS.length) verbosity = Level.ALL;
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #testArgsParser(String[], File, File,
	 * Level)}.
	 */
	private static Stream<Arguments> testArguments() throws Throwable {
		String[] inputFiles = {"src/test/resources/sample.pdf", "src/test/resources/sample.bla.pdf"};
		String[] outputFiles = {null, "../", "file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
		int[] verbosityLevels = IntStream.range(0, VERBOSITY_LEVELS.length + 1)
										 .toArray();
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				for (int lvl : verbosityLevels)
					list.add(createArgument(in, out, lvl));
		return list.stream();
	}
	
	/** Multiple tests related to using fromStringArray. */
	@Test
	void fromStringArray() {
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray((String) null));
		assertThrows(NullPointerException.class, () -> CLArgs.fromStringArray((String[]) null));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(new String[0]));
		assertThrows(IllegalArgumentException.class, CLArgs::fromStringArray);
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray("")
																 .getInput());
		
	}
	
	/** Checks if the arguments are passed into the corresponding expected values. */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}")
	@MethodSource("testArguments")
	void testArgsParser(String[] args, File input, File output, Level verbosity) {
		var clargs = CLArgs.fromStringArray(args);
		assertEquals(input, clargs.getInput());
		assertEquals(output, clargs.getOutput());
		assertEquals(verbosity, clargs.getVerbosity());
	}
	
	/** Multiple tests related to using getInput. */
	@Test
	void getInput() {
		var cla = CLArgs.fromStringArray("notExist.pdf");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("wrongType.txt");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("pom.xml");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("wrongType");
		assertThrows(IllegalArgumentException.class, cla::getInput);
	}
	
	/** Multiple tests related to using getOutput. */
	@Test
	void getOutput() {
		var cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "wrongType.txt");
		assertNull(cla.getOutput());
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "wrongType");
		assertNull(cla.getOutput());
	}
}