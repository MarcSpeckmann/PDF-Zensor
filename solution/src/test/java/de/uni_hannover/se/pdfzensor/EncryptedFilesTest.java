package de.uni_hannover.se.pdfzensor;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.PDFProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

class EncryptedFilesTest {
	private static Method openMethod = TestUtility
			.getPrivateMethod(App.class, "open", File.class, String.class, Integer.TYPE);
	
	@TestOnly
	private static PDDocument open(File file, String password, int tries) throws IOException {
		try {
			return (PDDocument) openMethod.invoke(null, file, password, tries);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail();
		} catch (InvocationTargetException e) {
			throw (IOException) e.getCause();
		}
		return null;
	}
	
	
	/** Tests if {@link App#open(File, String, int)}  does show expected behavior. */
	@Test
	void testOpen() {
		assertThrows(FileNotFoundException.class, () -> open(new File("notexisting.pdf"), null, 3));
		
		assertThrows(IOException.class, () -> open(new File("pom.xml"), null, 3));
		assertThrows(IOException.class, () -> open(
				new File(getResource(CORRUPTED_PDF_RESOURCE_PATH + "sample(pdfVersionDeleted).pdf").getPath()), null,
				3));
	}
	
	/**
	 * @param file the File which is passed to FileLoadingUtil.open().
	 * @throws IOException if there is an I/O-Error.
	 */
	@ParameterizedTest
	@ArgumentsSource(PDFProvider.class)
	void testOpenWithNonEncryptedFiles(File file) throws IOException {
		assertDoesNotThrow(() -> open(file, null, 0));
	}
	
	/**
	 * Tests if an encrypted PDF can be opened without an exception being thrown.
	 */
	@Test
	void testOpenWithEncryptedFile() {
		assertDoesNotThrow(() -> open(getResource(ENCRYPTED_PDF_RESOURCE_PATH + "Cryptography_Sample_encrypted.pdf"),
									  "testpassword", 3));
	}
}
