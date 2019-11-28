package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * Handles invalid syntax in command-line arguments.
 */
public class CLErrorMessageHandler implements CommandLine.IParameterExceptionHandler {
	
	/**
	 * Prints error message and synopsis on invalid syntax.
	 *
	 * @param ex   Exception which was thrown because of invalid command-line argument syntax
	 * @param args command-line arguments
	 * @return error code
	 */
	public int handleParseException(@NotNull CommandLine.ParameterException ex, String[] args) {
		CommandLine cmd = ex.getCommandLine();
		PrintWriter writer = cmd.getErr();
		
		writer.println(ex.getMessage());
		CommandLine.UnmatchedArgumentException.printSuggestions(ex, writer);
		
		writer.print(cmd.getHelp()
						.fullSynopsis());
		
		CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();
		writer.printf("%nTry '%s --help' for more information.%n", spec.qualifiedName());
		
		return cmd.getExitCodeExceptionMapper() != null
				? cmd.getExitCodeExceptionMapper()
					 .getExitCode(ex)
				: spec.exitCodeOnInvalidInput();
	}
}
