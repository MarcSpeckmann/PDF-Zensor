package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.censor.PDFCensor;
import de.uni_hannover.se.pdfzensor.config.CLErrorMessageHandler;
import de.uni_hannover.se.pdfzensor.config.CLHelp;
import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.processor.PDFProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * The main application.
 */
public class App {
	public static final String ROOT_DIR = System.getProperty("user.home") + "/.pdf-zensor/";
	
	private static Logger logger;
	
	/**
	 * Tries to open the (possibly password protected) pdf-file using the provided password. If that fails the user is
	 * prompted to provide the correct password. If he is unable to do so within <code>tries</code> attempts an {@link
	 * AuthenticationException} is thrown otherwise the opened {@link PDDocument} is returned.
	 *
	 * @param file the file to open.
	 * @param password the password that should be used for the initial try. May be <code>null</code>.
	 * @param tries the maximum amount of prompts given to the user for providing the correct password.
	 * @return the opened pdf-file.
	 * @throws IOException if an I/O error occurs.
	 * @throws AuthenticationException if the user failed to authenticate within <code>tries</code> attempts.
	 */
	@SuppressWarnings("squid:S106")// we explicitly want to print to stdout here instead of logging
	@NotNull
	private static PDDocument open(@NotNull File file, @Nullable String password, int tries) throws IOException {
		Objects.requireNonNull(file);
		password = Objects.requireNonNullElse(password, "");
		try (var reader = IOUtils.lineIterator(System.in, Charset.defaultCharset())) {
			for (int i = 0; i <= tries; i++) {
				try {
					var doc = PDDocument.load(file, password);
					doc.setAllSecurityToBeRemoved(true);
					return doc;
				} catch (InvalidPasswordException e) {
					if (i != tries) {
						System.out.println("Please provide a password!");
						password = reader.nextLine();
					}
				}
			}
		}
		logger.fatal("The user failed to provide authentication within {} attempts.", tries);
		throw new AuthenticationException();
	}
	
	/**
	 * Creates the {@link Settings}, {@link PDFCensor} and {@link PDFProcessor}.
	 *
	 * @param args Arguments given by the user via CL-input.
	 */
	@SuppressWarnings("squid:S106")// we explicitly want to print to stderr here instead of logging
	public static void main(String... args) {
		try {
			if (!CLHelp.printStandardHelpOptionsIfRequested(args)) {
				final var settings = new Settings(args);
				logger = Logging.getLogger();
				final var censor = new PDFCensor(settings);
				final var processor = new PDFProcessor(censor);
				final var tries = settings.getNoInteraction() ? 0 : 3;
				try (final var doc = open(settings.getInput(), settings.getPassword(), tries)) {
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
