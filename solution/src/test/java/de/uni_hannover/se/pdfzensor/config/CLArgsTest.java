package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.CLArgumentProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.util.ArrayList;

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
	
	/**
	 * Checks if the arguments are parsed into the corresponding expected values.
	 *
	 * @param args            The command-line arguments to parse.
	 * @param input           The expected input file as specified in args.
	 * @param output          The expected output file as specified in args.
	 * @param verbosity       The expected logger verbosity as specified in args.
	 * @param mode            The expected censoring mode as specified in args.
	 * @param expressions     The expected expressions as a list of string-string pairs as specified in args.
	 * @param quiet           The expected logger mode (whether or not it is expected to be silenced) as specified in
	 *                        args.
	 * @param intersectImages The expected behavior for overlapping text censor bars and censored images as specified in
	 *                        args.
	 */
	@ParameterizedTest(name = "Run {index}: args: {0} => in: {1}, out: {2}, verbosity: {3}, mode: {4}, expressions: {5}, quiet: {6}, intersectImages: {7}")
	@ArgumentsSource(CLArgumentProvider.class)
	void testArgsParser(@NotNull String[] args, @NotNull File input, @Nullable File output, @Nullable Level verbosity,
						@Nullable Mode mode, @NotNull ArrayList<ImmutablePair<String, String>> expressions,
						boolean quiet, boolean intersectImages) {
		var clArgs = CLArgs.fromStringArray(args);
		assertEquals(input, clArgs.getInput());
		assertEquals(output, clArgs.getOutput());
		assertEquals(verbosity, clArgs.getVerbosity());
		assertEquals(mode, clArgs.getMode());
		var actualExpressions = clArgs.getExpressions();
		assertNotNull(actualExpressions);
		assertEquals(expressions.size(), actualExpressions.length);
		for (var i = 0; i < expressions.size(); i++) {
			var expectedExp = new Expression(expressions.get(i).getLeft(), expressions.get(i).getRight());
			var actualExp = actualExpressions[i];
			assertEquals(expectedExp.getRegex(), actualExp.getRegex());
			assertEquals(expectedExp.getColor(), actualExp.getColor());
		}
		assertEquals(quiet, clArgs.getQuiet());
		assertEquals(intersectImages, clArgs.getIntersectImages());
	}
}