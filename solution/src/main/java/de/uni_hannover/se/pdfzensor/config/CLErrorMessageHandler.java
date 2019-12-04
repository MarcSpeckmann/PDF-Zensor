package de.uni_hannover.se.pdfzensor.config;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;

import static picocli.CommandLine.UnmatchedArgumentException.printSuggestions;

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
	public int handleParseException(CommandLine.ParameterException ex, String[] args) {
		Objects.requireNonNull(ex);
		CommandLine cmd = ex.getCommandLine();
		PrintWriter writer = cmd.getErr();
		
		writer.println(ex.getMessage());
		printSuggestions(ex, writer);
		
		writer.print(cmd.getHelp().fullSynopsis());
		
		var spec = cmd.getCommandSpec();
		writer.printf("%nTry '%s --help' for more information.%n", spec.qualifiedName());
		
		return Optional.ofNullable(cmd.getExitCodeExceptionMapper())
					   .map(e -> e.getExitCode(ex))
					   .orElseGet(spec::exitCodeOnInvalidInput);
	}
}
