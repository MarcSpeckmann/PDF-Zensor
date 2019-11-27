package de.uni_hannover.se.pdfzensor;


import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;

import java.io.IOException;

public class App {
	
	
	public static void main(String[] args) {
		int errorcode = CLHelp.checkValidArguments();
		if (errorcode != 0) {
			System.exit(errorcode);
		}
		if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
			try {
				Settings settings = new Settings("", args);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
