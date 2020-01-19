package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;


/**
 * FileLoadingUtil is a utility-class for opening PDFs and returning them.
 */
public final class FileLoadingUtil {
    /**
     * This constructor should not be called as no instance of {@link FileLoadingUtil} shall be created.
     *
     * @throws UnsupportedOperationException when called
     */
    private FileLoadingUtil() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

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
    public static PDDocument open(@NotNull File file, @Nullable String password, int tries) throws IOException {
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
        Logging.getLogger().fatal("The user failed to provide authentication within {} attempts.", tries);
        throw new AuthenticationException();
    }
}
