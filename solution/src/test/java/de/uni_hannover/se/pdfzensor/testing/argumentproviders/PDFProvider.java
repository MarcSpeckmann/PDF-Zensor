package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.PDF_RESOURCE_PATH;

/**
 * The PDFProvider is an ArgumentsProvider for Arguments that just take a file. The PDFProvider provides all files that
 * are in the resource directory {@link de.uni_hannover.se.pdfzensor.testing.TestConstants#PDF_RESOURCE_PATH}.
 */
public class PDFProvider implements ArgumentsProvider {
	
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var files = TestUtility.getResource(PDF_RESOURCE_PATH).listFiles(File::isFile);
		return Stream.of(Objects.requireNonNull(files)).map(Arguments::of);
	}
	
}
