package de.uni_hannover.se.pdfzensor.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.uni_hannover.se.pdfzensor.config.Settings.colorToString;
import static org.junit.jupiter.api.Assertions.*;


/** SettingsTest should contain all unit-tests related to {@link Settings}. */
class SettingsTest {
	
	/** Unit-tests for {@link Settings} constructor Settings */
	@Test
	void testSettings() {
		// if the command line argument is not given or has a faulty structure
		// following test should be right but test in CLArgsTest is not running!!
		// TODO: uncomment if CLArgsTest is fixed
		// assertThrows(NullPointerException.class, () -> new Settings(null));
		assertThrows(IllegalArgumentException.class, () -> new Settings(new String[2]));
		// if the command line argument is given but not valid
		// split uses whitespace as delimiter and splits the single string into an array of multiple strings for using it as an argument
		assertThrows(IllegalArgumentException.class, () -> new Settings("\"NichtExistenteDatei.pdf\""));
	}
}