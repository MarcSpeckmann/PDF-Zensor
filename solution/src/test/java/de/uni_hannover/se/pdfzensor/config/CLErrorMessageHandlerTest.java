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
		assertNotNull(new CLErrorMessageHandler());
		assertDoesNotThrow(() -> new CLErrorMessageHandler());
	}
	
	/**
	 * Testing if {@link CommandLine.ParameterException} is handled the right way.
	 */
	@Test
	@DisplayName("Test CLErrorMessageHandler output")
	void testhandleParseException() {
		//initialize handler
		final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		final PrintStream originalOut = System.err;
		System.setErr(new PrintStream(outContent));
		
		CLErrorMessageHandler handler = new CLErrorMessageHandler();
		CommandLine cmd = new CommandLine(CLArgs.class);
		
		
		CommandLine.ParameterException ex = new CommandLine.ParameterException(cmd, "Error");
		handler.handleParseException(ex, new String[]{});
		
		assertTrue(outContent.toString()
							 .contains("Error"));
		assertTrue(outContent.toString()
							 .contains(cmd.getHelp()
										  .fullSynopsis()));
		
		System.setOut(originalOut);
	}
}