package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * CLHelpTest should contain all unit-tests related to {@link CLHelp}.
 */
class CLHelpTest {
	
	/**
	 * Creates arguments for a function call of {@link #printStandardHelpOptionsIfRequested()} from the provided data.
	 */
	@NotNull
	private static Arguments createArgumentCLHelp(@NotNull String in, boolean help, boolean version,
												  boolean versionfirst) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (versionfirst) {
			if (version) arguments.add("-V");
			if (help) arguments.add("-h");
		} else {
			if (help) arguments.add("-h");
			if (version) arguments.add("-V");
		}
		var inFile = new File(in).getAbsoluteFile();
		
		return Arguments.of(arguments.toArray(new String[0]), inFile, help, version, versionfirst);
	}
	
	/**
	 * Provides a stream with the arguments for thorough testing of {@link #printStandardHelpOptionsIfRequested()}.
	 */
	private static Stream<Arguments> testArgumentsCLHelp() {
		String inputFile = "src/test/resources/sample.pdf";
		boolean[] bools = {true, false};
		
		var list = new ArrayList<Arguments>();
		for (boolean help : bools)
			for (boolean version : bools)
				for (boolean invert : bools)
					list.add(createArgumentCLHelp(inputFile, help, version, invert));
		return list.stream();
	}
	
	/**
	 * TODO: Add JavaDocs
	 */
	@Test
	void printStandardHelpOptionsIfRequested() {
		//TODO: implement tests
		fail("No tests implemented yet");
	}
}