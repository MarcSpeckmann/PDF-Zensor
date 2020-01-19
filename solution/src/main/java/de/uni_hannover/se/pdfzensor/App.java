package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.censor.PDFCensor;
import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import de.uni_hannover.se.pdfzensor.utils.FileLoadingUtil;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import java.io.IOException;
import java.util.Objects;

/**
 * The main application.
 */
public class App {
	private static Logger logger;

	/**
	 * Creates the {@link Settings}, {@link PDFCensor} and {@link PDFProcessor}.
	 * @param args Arguments given by the user via CL-input.
	 */
	@SuppressWarnings("squid:S106")// we explicitly want to print to stderr here instead of logging
	public static void main(String... args) {
		try {
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				final var settings = new Settings(null, args);
				logger = Logging.getLogger();
				final var censor = new PDFCensor(settings);
				final var processor = new PDFProcessor(censor);
				final var tries = settings.getNoInteraction()? 0:3;
				try (final var doc = FileLoadingUtil.open(settings.getInput(), settings.getPassword(), tries)) {
					processor.process(doc);
					doc.save(settings.getOutput());
				}
			}
		} catch (CommandLine.ParameterException ex) {
			CLErrorMessageHandler handler = new CLErrorMessageHandler();
			Objects.requireNonNullElseGet(logger, Logging::getLogger).error(ex);
			System.exit(handler.handleParseException(ex, args));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			Objects.requireNonNullElseGet(logger, Logging::getLogger).error(e);
			System.exit(-1);
		}
	}
}