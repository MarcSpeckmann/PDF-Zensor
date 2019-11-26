package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.ParseResult;

/**
 * The class is responsible for printing version and help if requested
 */
@Command(mixinStandardHelpOptions = true)
public final class CLHelp {
	/**
	 * Private constructor of {@link CLHelp}
	 */
	private CLHelp() {
	}
	
	/**
	 * Prints help or Version if -h or -V is given by the user
	 *
	 * @param args the command-line arguments which will be parsed
	 * @return true if help or version is requested
	 */
	public static boolean printStandardHelpOptionsIfRequested(String... args) {
		var cmd = new CommandLine(CLHelp.class);
		if (isVersionOrHelpRequested(cmd.parseArgs(args))) {
			if (cmd.isVersionHelpRequested())
				new CommandLine(CLArgs.class).printVersionHelp(System.out);
			if (cmd.isUsageHelpRequested())
				new CommandLine(CLArgs.class).usage(System.out, createColorScheme());
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if help or version is requested
	 *
	 * @param pr a parsed commando-line arguments
	 * @return true if help or version is requested
	 */
	private static boolean isVersionOrHelpRequested(@NotNull ParseResult pr) {
		return pr.isUsageHelpRequested() || pr.isVersionHelpRequested();
	}
	
	/**
	 * The color scheme is only enabled on ANSI-compatible consoles.
	 *
	 * @return The Ansi color scheme for coloring the help dialog in compatible command lines.
	 */
	@NotNull
	private static ColorScheme createColorScheme() {
		return new ColorScheme.Builder()
				.commands(Style.fg("0x0C"))
				.options(Style.fg("0xD6"))
				.optionParams(Style.fg("0xE2"))
				.parameters(Style.fg("0x09"))
				.build();
	}
}
