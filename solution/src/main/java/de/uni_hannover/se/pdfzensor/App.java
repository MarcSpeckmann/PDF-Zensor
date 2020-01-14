package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.censor.PDFCensor;
import de.uni_hannover.se.pdfzensor.utils.DecryptionUtil;
import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import picocli.CommandLine;

import java.io.IOException;


public class App {
	@SuppressWarnings("squid:S106")// we explicitly want to print to stderr here instead of logging
	public static void main(String... args) {
		try {
			final var settings = new Settings(null, args);
			boolean acceptPDF;
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				if(DecryptionUtil.isParsablePDF(settings)) {
					if (DecryptionUtil.isEncrypted(settings)) {
						acceptPDF = DecryptionUtil.handleEncryptedPDF(settings);
					} else{
						acceptPDF = true;
					}
					if (acceptPDF) {
						final var doc = PDDocument.load(settings.getInput(), settings.getPassword());
						if (doc.isEncrypted()) {
							doc.setAllSecurityToBeRemoved(true);
						}
						final var censor = new PDFCensor(settings);
						final var processor = new PDFProcessor(censor);
						processor.process(doc);
						doc.save(settings.getOutput());
					}
				} else {
					System.exit(-1);
				}
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
