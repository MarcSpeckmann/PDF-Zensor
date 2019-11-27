package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.PrintWriter;

public class CLErrorMessageHandler implements CommandLine.IParameterExceptionHandler {
	
	public int handleParseException(@NotNull CommandLine.ParameterException ex, String[] args) {
		CommandLine cmd = ex.getCommandLine();
		PrintWriter writer = cmd.getErr();
		
		writer.println(ex.getMessage());
		CommandLine.UnmatchedArgumentException.printSuggestions(ex, writer);
		//TODO: Print Synopsis
		/*writer.print(cmd.getHelp()
						.fullSynopsis()); */
		
		CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();
		writer.printf("Try '%s --help' for more information.%n", spec.qualifiedName());
		
		return cmd.getExitCodeExceptionMapper() != null
				? cmd.getExitCodeExceptionMapper()
					 .getExitCode(ex)
				: spec.exitCodeOnInvalidInput();
	}
}
