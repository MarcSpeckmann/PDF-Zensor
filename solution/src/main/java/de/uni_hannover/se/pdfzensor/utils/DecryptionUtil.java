package de.uni_hannover.se.pdfzensor.utils;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.config.Settings;
import org.apache.logging.log4j.Level;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import java.io.IOException;
import java.util.Scanner;

//TODO write javadoc
public final class DecryptionUtil {

    private DecryptionUtil(){
        //
    }

    //TODO: reduce complexity
    public static boolean handleEncryptedPDF(Settings settings){
        if(isCorrectPassword(settings)) return true;
        if(settings.getNoInteraction()){
            Logging.getLogger().log(Level.ERROR, "The document " + settings.getInput() + " is encrypted." +
                    " The password for decryption is either incorrect or missing.");
                    return true;
        }
        boolean done = false;
        String password;
        while(!done){
            password = getPasswordFromCL(settings);
            settings.setPassword(password);
            done = (password.equals("") || isCorrectPassword(settings));
        }
        return isCorrectPassword(settings);
    }

    /**
     * @param settings The {@link Settings} of the document which is analyzed.
     * @return a boolean that describes whether the PDF is encrypted (true) or not (false);
     */
    public static boolean isEncrypted(Settings settings){
        try (final var ignored = PDDocument.load(settings.getInput())){
            return false;
        } catch (InvalidPasswordException ipe) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isCorrectPassword(Settings settings){
        try (final var ignored = PDDocument.load(settings.getInput(), settings.getPassword())) {
            return true;
        } catch (InvalidPasswordException ipe) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }


    public static String getPasswordFromCL(Settings settings){
        Scanner scanner = new Scanner(System.in);
        Logging.getLogger().log(Level.ERROR, "Please enter the password for encrypted document {}", settings.getInput());
        Logging.getLogger().log(Level.ERROR, "(Just leave it blank if you want to skip this document!)");
        return scanner.nextLine();
    }

    public static boolean isParsablePDF(Settings settings){
        try(final var ignored = PDDocument.load(settings.getInput(), settings.getPassword())){
            return true;
        } catch (InvalidPasswordException ipe) {
            return true;
        } catch(IOException ex){
            return false;
        }
    }

}
