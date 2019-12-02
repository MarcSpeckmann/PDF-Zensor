package de.uni_hannover.se.pdfzensor.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/** This class implements {@link picocli.CommandLine.IVersionProvider} to provide the version for {@link CLHelp}. */
public final class VersionProvider implements CommandLine.IVersionProvider {
	
	/**
	 * Returns the text-lines that should be shown when the version is requested.
	 *
	 * @return a String[] containing the lines that should be printed and contain at least the version information.
	 * @throws IOException if project.properties can not be loaded because of an IO error.
	 */
	@NotNull
	@Contract(value = " -> new", pure = true)
	@Override
	public String[] getVersion() throws IOException {
		final var properties = new Properties();
		properties.load(Objects.requireNonNull(this.getClass().getResourceAsStream("/project.properties")));
		return new String[]{"PDF-Zensor",
				"Version: " + properties.getProperty("version"),
				"Build: " + properties.getProperty("timestamp")
		};
	}
}

