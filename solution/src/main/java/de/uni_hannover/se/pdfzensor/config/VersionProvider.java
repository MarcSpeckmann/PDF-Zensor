package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Properties;

public final class DummyVersionProvider implements CommandLine.IVersionProvider {
	static final String VERSION = "0.1";
	
	@NotNull
	@Contract(value = " -> new", pure = true)
	@Override
	public String[] getVersion() throws Exception {
		final Properties properties = new Properties();
		properties.load(this.getClass(). getClassLoader().getResourceAsStream("project.properties"));
		String[] version= {properties.getProperty("version")};
		return version;
	}
}

