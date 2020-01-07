package de.uni_hannover.se.pdfzensor.config;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

/** This class is responsible for printing the version and help if requested. */
public final class CLHelp {
	
	/**
	 * The default constructor should not be called and thus will always throw an exception when called.
	 *
	 * @throws UnsupportedOperationException when called.
	 */
	@Contract(value = " -> fail", pure = true)
	private CLHelp() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Prints the help- or version-string as they are defined in {@link CLArgs} to stdout. If the help-argument (-h or
	 * --help) is present in the arguments, the usage text is printed. If the version-argument (-V or --Version) is
	 * present in the arguments, the version-string for {@link CLArgs} is printed. If both are present only the usage is
	 * printed.
	 *
	 * @param args the command-line arguments which will be parsed
	 * @return true if help or version is requested
	 */
	@SuppressWarnings("squid:S106")// we explicitly want to print to stdout here instead of logging
	public static boolean printStandardHelpOptionsIfRequested(@NotNull String... args) {
		final var cmd = new CommandLine(CLArgs.class);
		final var parsedHelp = new CommandLine(CLArgs.class).parseArgs(args);
		if (parsedHelp.isUsageHelpRequested())
			cmd.usage(System.out);
		else if (parsedHelp.isVersionHelpRequested())
			cmd.printVersionHelp(System.out);
		else return false;
		return true;
	}
}
