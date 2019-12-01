package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.utils.Utils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

public class CLArgumentProvider implements ArgumentsProvider {
	private static final String[] inputFiles = {"/pdf-files/sample.pdf", "/pdf-files/sample.bla.pdf"};
	private static final String[] outputFiles = {null, "file.pdf", "src/test/resources/sample.pdf", "weirdSuffix.bla.pdf"};
	private static final int[] verbosityLevels = IntStream.range(0, VERBOSITY_LEVELS.length + 1).toArray();
	
	
	@NotNull
	private static Arguments createArgument(@NotNull String in, @Nullable String out, final int lvl) {
		var arguments = new ArrayList<String>();
		arguments.add(in);
		if (out != null) {
			arguments.add("-o");
			arguments.add(out);
		}
		Level verbosity = null;
		if (lvl > 0) {
			arguments.add("-" + "v".repeat(lvl));
			verbosity = VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, lvl)];
		}
		
		var inFile = new File(in);
		var outFile = Optional.ofNullable(out).map(File::new).orElse(null);
		return Arguments.of(arguments.toArray(new String[0]), inFile, outFile, verbosity);
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		for (String in : inputFiles)
			for (String out : outputFiles)
				for (int lvl : verbosityLevels)
					list.add(createArgument(in, out, lvl));
		return list.stream();
	}
	
}
