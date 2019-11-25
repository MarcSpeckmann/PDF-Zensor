package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.ParseResult;

/**
 * TODO: ADD JavaDoc
 */
@Command(mixinStandardHelpOptions = true)
public final class CLHelp {
	/**
	 * TODO: ADD JavaDoc
	 *
	 * @param args
	 * @return
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
	 * TODO: ADD JavaDoc
	 *
	 * @param pr
	 * @return
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
