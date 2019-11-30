package de.uni_hannover.se.pdfzensor.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PDFProcessorTest {
	
	// TODO: how to test the constructor and {@link #process} without having a working or initializable handler??
	
	@Test
	void testIllegalArguments()  {
		assertThrows(NullPointerException.class, () -> new PDFProcessor(null));
	}
	
	@Test
	void process() {
	}
}