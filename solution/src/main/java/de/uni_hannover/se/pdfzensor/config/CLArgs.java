package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static de.uni_hannover.se.pdfzensor.utils.Utils.fitToArray;

/**
 * The class is responsible for parsing the given command-line arguments
 *
 * @author Marc Speckmann
 * @author Lennart Bohlin
 */
@Command(name = "pdf-zensor", version = DummySettings.version, description = {"--Here could be your description--"})
public final class CLArgs {
	
	@Option(names = {"-v", "--verbose"}, description = {"Specify multiple -v options to increase verbosity."}, arity = "0")
	@Nullable
	private boolean[] verbose = null;
	
	/**
	 * This method is parsing the command-line arguments.
	 *
	 * @param args the command-line arguments which will be parsed
	 * @return an CLArgs object which contains all information about the parsed arguments
	 */
	@NotNull
	public static CLArgs fromStringArray(@NotNull final String[] args) {
		final CLArgs clArgs = new CLArgs();
		final CommandLine cmd = new CommandLine(clArgs);
		cmd.parseArgs(Objects.requireNonNull(args));
		return clArgs;
	}
	
	/**
	 * Returns verbosity level given bei the user
	 *
	 * @return null or the level of logging verbosity if verbose was given
	 */
	@Contract(pure = true)
	@Nullable
	final Level getVerbosity() {
		return verbose == null ? null : VERBOSITY_LEVELS[fitToArray(VERBOSITY_LEVELS, verbose.length)];
	}
	
}
