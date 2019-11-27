package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParseResult;

/**
 * This class is responsible for printing version and help if requested.
 */
@Command(mixinStandardHelpOptions = true)
public final class CLHelp {
	/**
	 * The default constructor should not be called and thus will always throw an exception when called.
	 */
	@Contract(pure = true)
	private CLHelp() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Prints help or version if either -h or -V is present in the arguments.
	 *
	 * @param args the command-line arguments which will be parsed
	 * @return true if help or version is requested
	 */
	public static boolean printStandardHelpOptionsIfRequested(String... args) {
		var cmd = new CommandLine(CLArgs.class);
		if (isVersionOrHelpRequested(cmd.setParameterExceptionHandler(new CLErrorMessageHandler()).parseArgs(args))) {
			if (cmd.isUsageHelpRequested()) {
				new CommandLine(CLArgs.class).usage(System.out);
			} else if (cmd.isVersionHelpRequested()) {
				new CommandLine(CLArgs.class).printVersionHelp(System.out);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if help or version is requested.
	 *
	 * @param pr parsed commando-line arguments
	 * @return true if help or version is requested
	 */
	private static boolean isVersionOrHelpRequested(@NotNull ParseResult pr) {
		return pr.isUsageHelpRequested() || pr.isVersionHelpRequested();
	}

}
