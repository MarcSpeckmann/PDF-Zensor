package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.NoSuchElementException;

import static de.uni_hannover.se.pdfzensor.Logging.VERBOSITY_LEVELS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CLArgsTest should contain all unit-tests related to {@link CLArgs}.
 */
class CLArgsTest {
	
	/**
	 * Multiple tests related to using fromStringArray
	 */
	@Test
	void fromStringArray() {
		assertThrows(NullPointerException.class, () -> CLArgs.fromStringArray(null));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(new String[0]));
		assertThrows(IllegalArgumentException.class, CLArgs::fromStringArray);
		assertThrows(NoSuchElementException.class, () -> CLArgs.fromStringArray("")
															   .getInput());
		
	}
	
	/**
	 * Multiple tests related to using getVerbosity
	 */
	@Test
	void getVerbosity() {
		CLArgs cla;
		
		for (int i = 1; i < VERBOSITY_LEVELS.length; i++) {
			cla = CLArgs.fromStringArray("sample.pdf", "-" + "v".repeat(i));
			assertEquals(VERBOSITY_LEVELS[i], cla.getVerbosity());
		}
		
		cla = CLArgs.fromStringArray("sample.pdf", "-" + "v".repeat(VERBOSITY_LEVELS.length - 1));
		assertEquals(Level.ALL, cla.getVerbosity());
		
		cla = CLArgs.fromStringArray("sample.pdf", "-" + "v".repeat(VERBOSITY_LEVELS.length));
		assertEquals(Level.ALL, cla.getVerbosity());
	}
	
	@Test
	void getInput() {
		var cla = CLArgs.fromStringArray("notExist.pdf");
		assertThrows(NoSuchElementException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf");
		assertEquals(new File("src/test/resources/sample.pdf").getAbsoluteFile(), cla.getInput());
		
		cla = CLArgs.fromStringArray("wrongTyp.txt");
		assertThrows(NoSuchElementException.class, cla::getInput);
		//TODO: test for multiple types not for .txt
	}
	
	@Test
	void getOutput() {
		//save in directory
		var cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "../");
		assertEquals(new File("../").getAbsoluteFile(), cla.getOutput());
		//test different order
		cla = CLArgs.fromStringArray("-o", "../", "src/test/resources/sample.pdf");
		assertEquals(new File("../").getAbsoluteFile(), cla.getOutput());
		//save in file
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "file.pdf");
		assertEquals(new File("file.pdf").getAbsoluteFile(), cla.getOutput());
		//safe in same file
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "src/test/resources/sample.pdf");
		assertEquals(new File("sample.pdf").getAbsoluteFile(), cla.getOutput());
		//safe in different format then .pdf
		final var cla2 = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "src/test/resources/sample.txt");
		assertThrows(Exception.class, () -> cla2.getOutput());
	}
}