package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.censor.PDFCensor;
import de.uni_hannover.se.pdfzensor.censor.utils.PasswordUtil;
import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import org.apache.logging.log4j.Level;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import picocli.CommandLine;

import java.io.IOException;

public class App {
	@SuppressWarnings("squid:S106")// we explicitly want to print to stderr here instead of logging
	public static void main(String... args) {
		try {
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				final var settings = new Settings(null, args);
				final var censor = new PDFCensor(settings);
				final var processor = new PDFProcessor(censor);
				boolean done = false;
				while(!done) {
					Logging.getLogger().log(Level.ERROR, "I am at the beginning of the while-loop!");
					try (final var doc = PDDocument.load(settings.getInput(), settings.getPassword())) {
						if (doc.isEncrypted()) {
							doc.setAllSecurityToBeRemoved(true);
						}
						processor.process(doc);
						doc.save(settings.getOutput());
						done = true;
					} catch (InvalidPasswordException ipe) {
						done = PasswordUtil.handleIncorrectPassword(settings);
						//System.err.println(ipe.getMessage());
						//Logging.getLogger().error(ipe);
						//System.exit(-1);
						Logging.getLogger().log(Level.ERROR, "I am inside the catch-block! done = " + done);
					}
					Logging.getLogger().log(Level.ERROR, "I am at the end of the while-loop!");
				}
				Logging.getLogger().log(Level.ERROR, "I am outside of the while-loop!");
			}
		} catch (CommandLine.ParameterException ex) {
			CLErrorMessageHandler handler = new CLErrorMessageHandler();
			Logging.getLogger().error(ex);
			System.exit(handler.handleParseException(ex, args));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			Logging.getLogger().error(e);
			System.exit(-1);
		}
	}
}
