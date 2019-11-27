package de.uni_hannover.se.pdfzensor;


import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import picocli.CommandLine;

import java.io.IOException;

public class App {
	
	public static void main(String[] args) {
		try {
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				
				Settings settings = new Settings("", args);
				
			}
		} catch (CommandLine.ParameterException ex) {
			CLErrorMessageHandler handler = new CLErrorMessageHandler();
			System.exit(handler.handleParseException(ex, args));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
