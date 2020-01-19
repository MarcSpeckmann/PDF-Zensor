package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.config.Settings;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.testing.TestConstants.*;
import static de.uni_hannover.se.pdfzensor.testing.TestUtility.getResource;
import static org.junit.jupiter.api.Assertions.*;

class FileLoadingUtilTest {

    /**
     * Tests if {@link FileLoadingUtil}.open(...) does show expected behaviour.
     */
    @Test
    void testOpen() {
        assertThrows(FileNotFoundException.class,() -> FileLoadingUtil.open(new File("notexisting.pdf"), null, 3));

        assertThrows(IOException.class,() -> FileLoadingUtil.open(new File("pom.xml"), null, 3));
        assertThrows(IOException.class,
        () -> FileLoadingUtil.open(new File(getResource(CORRUPTED_PDF_RESOURCE_PATH + "sample(pdfVersionDeleted).pdf").getPath()), null, 3));
    }

    /**
     * @return the {@link Stream} that contains the PDF-files from the resource-folder
     * @throws IOException when there is an I/O-Error.
     */
    private static Stream<Arguments> testNonEncryptedArguments() throws IOException {
        return Files.walk(Paths.get(TestUtility
                .getResource(PDF_RESOURCE_PATH).getAbsolutePath()))
                .map(Path::toFile)
                .filter(File::isFile)
                .map(Arguments::of);
    }

    /**
     * @param file the File which is passed to FileLoadingUtil.open().
     * @throws IOException if there is an I/O-Error.
     */
    @ParameterizedTest
    @MethodSource("testNonEncryptedArguments")
    void testOpenWithNonEncryptedFiles(File file) throws IOException{
        assertDoesNotThrow(() -> FileLoadingUtil.open(file, null,0));
    }

    /**
     * Tests if an encrypted PDF can be opened without an exception being thrown.
     */
    @Test
    void testOpenWithEncryptedFile(){
        assertDoesNotThrow(() -> FileLoadingUtil.open(getResource(ENCRYPTED_PDF_RESOURCE_PATH + "Cryptography_Sample_encrypted.pdf"), "testpassword",3));
    }
}
