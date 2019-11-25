package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Objects;
import java.util.Properties;

/**
 * TODO: Add Javadoc
 */
public final class VersionProvider implements CommandLine.IVersionProvider {
	
	/**
	 * TODO: Add JavaDoc
	 * @return
	 * @throws Exception
	 */
	@NotNull
	@Contract(value = " -> new", pure = true)
	@Override
	public String[] getVersion() throws Exception {
		final Properties properties = new Properties();
		properties.load(Objects.requireNonNull(this.getClass()
												   .getClassLoader()
												   .getResourceAsStream("project.properties")));
		String[] version = {properties.getProperty("version")};
		return version;
	}
}

