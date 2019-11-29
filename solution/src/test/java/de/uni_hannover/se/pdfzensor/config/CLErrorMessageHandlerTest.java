package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link CLErrorMessageHandler}.
 */
class CLErrorMessageHandlerTest {
	
	/**
	 * Testing if the constructor of {@link CLErrorMessageHandler} is working.
	 */
	@Test
	@DisplayName("Test CLErrorMessageHandler constructor")
	void testConstructor() {
		assertDoesNotThrow(CLErrorMessageHandler::new);
	}
	
	/**
	 * Testing if {@link CommandLine.ParameterException} is handled the right way.
	 */
	@Test
	@DisplayName("Test CLErrorMessageHandler output")
	void testhandleParseException() {
		//redirect System.err to ByteArrayOutputStream
		final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		final PrintStream originalOut = System.err;
		System.setErr(new PrintStream(outContent));
		
		CLErrorMessageHandler handler = new CLErrorMessageHandler();
		CommandLine cmd = new CommandLine(CLArgs.class);
		
		//check null input
		assertThrows(NullPointerException.class, () -> handler.handleParseException(null, null));
		assertThrows(NullPointerException.class, () ->handler.handleParseException(null, new String[]{}));
		
		//construct ParameterException
		var errmsg = "Error";
		CommandLine.ParameterException ex = new CommandLine.ParameterException(cmd, errmsg);
		
		assertDoesNotThrow(()->handler.handleParseException(ex, null));
		
		//TODO: possibly add more tests and change @Test to @ParameterizedTest
		//string can be empty because it is not used inside the method handleParseException
		//return isn't allowed to bei 0, because an exception occurred
		assertTrue(handler.handleParseException(ex, new String[]{}) != 0);
		
		assertTrue(outContent.toString()
							 .contains(errmsg));
		assertTrue(outContent.toString()
							 .contains(cmd.getHelp()
										  .fullSynopsis()));
		//check if '--help' is mentioned inside the error message
		assertTrue(outContent.toString().contains("--help"));
		
		System.setOut(originalOut);
	}
}