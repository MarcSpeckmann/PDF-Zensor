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
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray((String) null));
		assertThrows(NullPointerException.class, () -> CLArgs.fromStringArray((String[]) null));
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray(new String[0]));
		assertThrows(IllegalArgumentException.class, CLArgs::fromStringArray);
		assertThrows(IllegalArgumentException.class, () -> CLArgs.fromStringArray("")
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
	
	/**
	 * Multiple tests related to using getInput
	 */
	@Test
	void getInput() {
		var cla = CLArgs.fromStringArray("notExist.pdf");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf");
		assertEquals(new File("src/test/resources/sample.pdf").getAbsoluteFile(), cla.getInput());
		
		cla = CLArgs.fromStringArray("wrongType.txt");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("pom.xml");
		assertThrows(IllegalArgumentException.class, cla::getInput);
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.bla.pdf");
		assertEquals(new File("src/test/resources/sample.bla.pdf").getAbsoluteFile(), cla.getInput());
		
		cla = CLArgs.fromStringArray("wrongType");
		assertThrows(IllegalArgumentException.class, cla::getInput);
	}
	
	/**
	 * Multiple tests related to using getOutput
	 */
	@Test
	void getOutput() {
		
		var cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "../");
		assertEquals(new File("../").getAbsoluteFile(), cla.getOutput());
		
		cla = CLArgs.fromStringArray("-o", "../", "src/test/resources/sample.pdf");
		assertEquals(new File("../").getAbsoluteFile(), cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "file.pdf");
		assertEquals(new File("file.pdf").getAbsoluteFile(), cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "src/test/resources/sample.pdf");
		assertEquals(new File("src/test/resources/sample.pdf").getAbsoluteFile(), cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf");
		assertNull(cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "wrongType.txt");
		assertNull(cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "wrongType.bla.pdf");
		assertEquals(new File("wrongType.bla.pdf").getAbsoluteFile(), cla.getOutput());
		
		cla = CLArgs.fromStringArray("src/test/resources/sample.pdf", "-o", "wrongType");
		assertNull(cla.getOutput());
	}
}