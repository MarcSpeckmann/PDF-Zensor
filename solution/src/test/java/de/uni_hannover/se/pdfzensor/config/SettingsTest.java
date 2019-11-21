package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {

	/** Unit-tests for {@link Settings} constructor Settings */
	@Test
	void testSettings(){
        // if the command line argument is not given or has a faulty structure
		//TODO the following test does not Work yet
		//assertThrows(NullPointerException.class, () -> new Settings(null));
        assertThrows(IllegalArgumentException.class, () -> new Settings(new String[2]));
        // if the command line argument is given but not valid
        // split uses whitespace as delimiter and splits the single string into an array of multiple strings for using it as an argument
        assertThrows(picocli.CommandLine.UnmatchedArgumentException.class, () -> new Settings("pdf-zensor \"NichtExistenteDatei.pdf\"".split(" ")));
        // for this test there has to be a zensieren.pdf file in the same directory but no config.json
        assertThrows(picocli.CommandLine.UnmatchedArgumentException.class, () -> new Settings("pdf-zensor \"zensieren.pdf\" -c \"config.json\"".split(" ")));
	}

	/** Unit-tests for {@link Settings} function getColorOrNull */
	@Test
	void getColorOrNull() {
		// if the parameter is null
		assertNull(Settings.getColorOrNull(null));
		
		//if the parameter is a valid hexadecimal color code with 6 letters beginning with '#'
		assertEquals(Color.BLACK, Settings.getColorOrNull("#000000"));
		assertEquals(Color.RED, Settings.getColorOrNull("#ff0000"));
		assertEquals(Color.GREEN, Settings.getColorOrNull("#00ff00"));
		assertEquals(Color.BLUE, Settings.getColorOrNull("#0000ff"));
		assertEquals(Color.YELLOW, Settings.getColorOrNull("#ffff00"));
		assertEquals(Color.CYAN, Settings.getColorOrNull("#00ffff"));
		assertEquals(Color.WHITE, Settings.getColorOrNull("#ffffff"));
		
		//if the parameter is a valid hexadecimal color code with 3 letters beginning with '#'
		assertEquals(Color.BLACK, Settings.getColorOrNull("#000"));
		assertEquals(Color.RED, Settings.getColorOrNull("#f00"));
		assertEquals(Color.GREEN, Settings.getColorOrNull("#0f0"));
		assertEquals(Color.BLUE, Settings.getColorOrNull("#00f"));
		assertEquals(Color.YELLOW, Settings.getColorOrNull("#ff0"));
		assertEquals(Color.CYAN, Settings.getColorOrNull("#0ff"));
		assertEquals(Color.WHITE, Settings.getColorOrNull("#fff"));
		
		// if the parameter is a valid hexadecimal color with 6 letters beginning with '0x'
		assertEquals(Color.BLACK, Settings.getColorOrNull("0x000000"));
		assertEquals(Color.RED, Settings.getColorOrNull("0xff0000"));
		assertEquals(Color.GREEN, Settings.getColorOrNull("0x00ff00"));
		assertEquals(Color.BLUE, Settings.getColorOrNull("0x0000ff"));
		assertEquals(Color.YELLOW, Settings.getColorOrNull("0xffff00"));
		assertEquals(Color.CYAN, Settings.getColorOrNull("0x00ffff"));
		assertEquals(Color.WHITE, Settings.getColorOrNull("0xffffff"));
		// the same with 'OX'
		assertEquals(Color.BLACK, Settings.getColorOrNull("0X000000"));
		assertEquals(Color.RED, Settings.getColorOrNull("0Xff0000"));
		assertEquals(Color.GREEN, Settings.getColorOrNull("0X00ff00"));
		assertEquals(Color.BLUE, Settings.getColorOrNull("0X0000ff"));
		assertEquals(Color.YELLOW, Settings.getColorOrNull("0Xffff00"));
		assertEquals(Color.CYAN, Settings.getColorOrNull("0X00ffff"));
		assertEquals(Color.WHITE, Settings.getColorOrNull("0Xffffff"));
		
		//if the parameter is a valid hexadecimal color code with 3 letters beginning with '0x'
		assertEquals(Color.BLACK, Settings.getColorOrNull("0x000"));
		assertEquals(Color.RED, Settings.getColorOrNull("0xf00"));
		assertEquals(Color.GREEN, Settings.getColorOrNull("0x0f0"));
		assertEquals(Color.BLUE, Settings.getColorOrNull("0x00f"));
		assertEquals(Color.YELLOW, Settings.getColorOrNull("0xff0"));
		assertEquals(Color.CYAN, Settings.getColorOrNull("0x0ff"));
		assertEquals(Color.WHITE, Settings.getColorOrNull("0xfff"));
		assertEquals(Color.DARK_GRAY, Settings.getColorOrNull("#404040"));

		// some random colors
		assertEquals(new Color(130,150,161), Settings.getColorOrNull("#8296A1"));
		assertEquals(new Color(77,52,67), Settings.getColorOrNull("0x4D3443"));
		assertEquals(new Color(18, 10, 77), Settings.getColorOrNull("0x120A4D"));
		assertEquals(new Color(18, 52, 86), Settings.getColorOrNull("0x123456"));
		assertEquals(new Color(3, 77, 31), Settings.getColorOrNull("0x034D1F"));
		assertEquals(new Color(77, 76, 27), Settings.getColorOrNull("0x4D4C1B"));
		assertEquals(new Color(86, 42, 86), Settings.getColorOrNull("0x562A56"));
		assertEquals(new Color(250, 204, 204), Settings.getColorOrNull("#FACCCC"));
		// if the parameter is no valid hexadecimal color code
		// TODO
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("BLACK"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#f"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ffff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#fffff"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#ffffgg"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("0xffffgg"));
		assertThrows(IllegalArgumentException.class, () -> Settings.getColorOrNull("#fffffff"));
	}

	/*
	* Unit-tests for {@link colorToString}
	*  */
	@Test
	void colorToString() {
		
		// if the parameter is a Color class
		assertEquals("#000000", Settings.colorToString(Color.BLACK));
		assertEquals("#FF0000", Settings.colorToString(Color.RED));
		assertEquals("#00FF00", Settings.colorToString(Color.GREEN));
		assertEquals("#0000FF", Settings.colorToString(Color.BLUE));
		assertEquals("#FFFF00", Settings.colorToString(Color.YELLOW));
		assertEquals("#00FFFF", Settings.colorToString(Color.CYAN));
		assertEquals("#FFFFFF", Settings.colorToString(Color.WHITE));
	}
}