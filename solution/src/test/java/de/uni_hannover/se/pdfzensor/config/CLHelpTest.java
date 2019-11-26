package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLHelpTest should contain all unit-tests related to {@link CLHelp}.
 */
class CLHelpTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	
	@BeforeAll
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
	}
	
	@AfterAll
	public void restoreStreams() {
		System.setOut(originalOut);
	}
	 
	 /** Tests if a call including -V or -h prints text.
	 */
	@ParameterizedTest
	void printStandardHelpOptionsIfRequested() {
		assertNotNull(CLHelp.printStandardHelpOptionsIfRequested());
		assertTrue(outContent.toString().length() > 10);
	}
}