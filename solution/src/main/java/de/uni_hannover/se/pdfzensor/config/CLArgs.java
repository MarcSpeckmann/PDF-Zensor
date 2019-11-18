package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Objects;

/**
 * The class is responsible for parsing the given commando line
 *
 * @author Marc Speckmann
 * @author Lennart Bohlin
 */
@Command(name = "pdf-zensor", version = "0.1", description = {"--Here could be your description--"})
final public class CLArgs {
	
	@Option(names = {"-v", "--verbose"}, description = {"Specify multiple -v options to increase verbosity."}, arity = "0")
	@Nullable
	private boolean[] verbose = null;
	
	/**
	 * This mathod is parsing the commando line.
	 *
	 * @param args the commando line which is going to be parsed
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
	 * @return null or the level of logging verbostity if verbose was given
	 */
	@Contract(pure = true)
	@Nullable
	final Level getVerbosity() {
		return Level.OFF;
	}
	
}
