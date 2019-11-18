package de.uni_hannover.se.pdfzensor.config;

import org.apache.logging.log4j.Level;

import java.io.File;

final class ConfigParser {
	
	private ConfigParser() {
		this(null, null);
	}
	private ConfigParser(final File out, final Object verbose) {
		return;
	}
	
	static ConfigParser fromFile(final File config) {
		return null;
	}
	
	File getOutput() {
		return null;
	}
	
	Level getVerbosity() {
		return null;
	}
}